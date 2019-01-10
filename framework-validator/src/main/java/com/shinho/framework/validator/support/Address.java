package com.shinho.framework.validator.support;


import com.google.common.base.Strings;
import com.shinho.framework.base.utils.StringUtils;
import com.shinho.framework.validator.utils.ValidateUtils;

/*
 * Author:   林晓辉
 * Date:     14-12-26
 * Description: 模块目的、功能描述      
 * History: 变更记录
 * <author>           <time>             <version>        <desc>
 * 林晓辉           14-12-26           00000001         创建文件
 *
 */
public class Address {

    private String province="";
    private String city="";
    private String district="";
    private String code;

    public Address(String code) {

        if( null==code || code.trim().length()!=6 || !ValidateUtils.isDigit(code.trim())  ) {
            throw new IllegalArgumentException("错误的区域编码,区域编码为6位整数");
        }

        this.code = code;
    }

    public String getProvince() {
        return province;
    }

    public String getProvinceCode() {
        return StringUtils.padEnd(code.substring(0, 2), 6, '0');
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public String getCityCode() {
        return StringUtils.padEnd(code.substring(0, 4), 6, '0');
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return province + city + district;
    }

    public String toString(String delimiter) {

        StringBuffer stringBuffer = new StringBuffer(province);
        if(!Strings.isNullOrEmpty(city) ){
            stringBuffer.append(delimiter).append(city);
        }
        if(!Strings.isNullOrEmpty(district) ){
            stringBuffer.append(delimiter).append(district);
        }
        return stringBuffer.toString();
    }
}
