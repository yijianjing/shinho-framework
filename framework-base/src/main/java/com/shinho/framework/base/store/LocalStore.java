package com.shinho.framework.base.store;

import com.google.common.cache.*;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 未测试
 * Created by linxiaohui on 2017/7/24.
 */
public class LocalStore implements Store{

    private static final Object LOCK = new Object();
    private static final Object NULL_OBJECT = new Object();

    private static Integer MAX_MUM_SIZE = 50000;



    /** 过期时间 map(key,过期的时间,value要过期的key集合)  **/
    private static final Map<Long,Set<String>> EXPIRE_TIME_MAP = Maps.newConcurrentMap();
    /** 过期时间 map(key,要过期的 key,value 过期的时间) **/
    private static final Map<String,Long> EXPIRE_KEY = Maps.newTreeMap();

    private static final RemovalListener removalListener = (n) -> {

        //COLLECTED：垃圾回收引起键值被自动清除，在使用weakKeys，weakValues 或 softValues时， 会发生这种情况 。
        //EXPIRED：键值过期，在使用expireAfterAccess 或 expireAfterWrite时，会发生这种情况。
        //SIZE：缓存大小限制引起键值被清除，在使用maximumSize 或 maximumWeight时，会发生这种情况
        if( n.getCause() == RemovalCause.COLLECTED
                || n.getCause() == RemovalCause.EXPIRED
                || n.getCause() == RemovalCause.SIZE ){

            String key = (String)n.getKey();
            remove(key);
        }

    };
    /** hash 类型的 Cache **/
    private static final LoadingCache<String,Map<String,Object>> LOCAL_HASH_CACHE = CacheBuilder.newBuilder()
            .maximumSize(MAX_MUM_SIZE)
            .removalListener(removalListener)
            .build(new CacheLoader<String, Map<String,Object>>() {
                @Override
                public Map load(String key) throws Exception {
                    return Maps.newHashMap();
                }
            });

    /** 普通类型的 cache **/
    private static final LoadingCache<String,Object> LOCAL_CACHE = CacheBuilder.newBuilder()
            .maximumSize(MAX_MUM_SIZE)
            .removalListener(removalListener)
            .build(new CacheLoader<String, Object>() {
                @Override
                public Object load(String key) throws Exception {
                    return NULL_OBJECT;
                }
            });

    @Override
    public Object get(String key) {
        expire();
        Object value = LOCAL_CACHE.getUnchecked(key);
        if( value == NULL_OBJECT){
            return null;
        }
        return value;
    }

    @Override
    public void set(String key, Object value) {
        expire();
        LOCAL_CACHE.put(key,value);
    }

    @Override
    public void delete(String key) {
        invalidate(key);
    }

    @Override
    public String[] keys(String domainKey) {
        return LOCAL_HASH_CACHE.getUnchecked(domainKey).keySet().toArray(new String[]{});
    }

    @Override
    public void put(String domainKey, String key, Object value) {

        expire();
        LOCAL_HASH_CACHE.getUnchecked(domainKey).put(key,value);
    }

    @Override
    public void delete(String domainKey, String key) {

        LOCAL_HASH_CACHE.getUnchecked(domainKey).remove(key);
    }

    @Override
    public void invalidate(String key) {

        LOCAL_CACHE.invalidate(key);
        LOCAL_HASH_CACHE.invalidate(key);
        remove(key);
    }

    @Override
    public Object get(String domainKey, String key) {

        expire();
        return LOCAL_HASH_CACHE.getUnchecked(domainKey).get(key);
    }

    @Override
    public <T> T get(String domainKey, String key, T defaultValue) {

        expire();
        Object value = LOCAL_HASH_CACHE.getUnchecked(domainKey).get(key);
        if( null == value ){
            return defaultValue;
        }
        return (T)value;
    }

    @Override
    public Map<String, Object> entries(String domainKey) {
        return LOCAL_HASH_CACHE.getUnchecked(domainKey);
    }

    @Override
    public void expire(String key, long timeOut, TimeUnit timeUnit) {

        if( !hasKey(key) ){
            return;
        }

        //上锁
        synchronized ( LOCK ){

            if( hasKey(key) ){
                Set<String> set = EXPIRE_TIME_MAP.get(key);
                Long expire = System.currentTimeMillis();
                if( null == set ){
                    set = Sets.newHashSet();
                    expire += TimeUnit.MILLISECONDS.convert(timeOut,timeUnit);
                    EXPIRE_TIME_MAP.put(expire,set);
                }
                set.add(key);
                EXPIRE_KEY.put(key,expire);
            }
        }
    }

    @Override
    public boolean hasKey(String domainKey) {
        return null != LOCAL_CACHE.getUnchecked(domainKey) || null != LOCAL_HASH_CACHE.getUnchecked(domainKey);
    }

    @Override
    public int order() {
        return 0;
    }


    /**
     * 主动让过期对象失效
     */
    public void expire(){

        //检测
        if( EXPIRE_TIME_MAP.isEmpty() ){
            return;
        }

        //上锁
        synchronized ( LOCK ){

            //二次检测
            if( EXPIRE_TIME_MAP.isEmpty() ){
                return;
            }

            //转为数组
            Long[] expireArray = EXPIRE_TIME_MAP.keySet().toArray(new Long[]{});
            //当前时间
            Long now = System.currentTimeMillis();
            //二分查找法,查找最接近过期的索引
            int index = binarySearch(expireArray,now);
            //循环
            for(int i=0; i<index ;i++){

                Long expire  = expireArray[i];
                if( expire <= now ){

                    Set<String> removeKeys = EXPIRE_TIME_MAP.remove(expire);
                    for(String removeKey : removeKeys){
                        invalidate(removeKey);
                        EXPIRE_KEY.remove(removeKey);

                    }
                }

            }
        }
    }

    private static void remove(String key){

        Long expire = EXPIRE_KEY.remove(key);
        if( null != expire ){
            Set s = EXPIRE_TIME_MAP.get(key);
            if( null != s){
                s.remove(key);
            }
        }
    }
    /**
     * 二分查找法,查询最近的索引
     * @param a
     * @param key
     * @return
     */
    private static int binarySearch(Long[] a, long key) {
        int low = 0;
        int high = a.length - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            long midVal = a[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found

        }
        return low;
//        return -(low + 1);  // key not found.
    }

    public static Integer getMaxMumSize() {
        return MAX_MUM_SIZE;
    }

    public static void setMaxMumSize(Integer maxMumSize) {
        MAX_MUM_SIZE = maxMumSize;
    }
}
