package com.shinho.framewok.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MultiTenantDataSourceProxy extends AbstractMultiTenantDataSourceProxy {


    @Autowired(required = false)
    private TenantConfigMapping tenantConfigMapping;

    private Map<String,DataSource> dataSourceMapping = new ConcurrentHashMap<String,DataSource>();

    public Map<String, DataSource> getDataSourceMapping() {
        return dataSourceMapping;
    }

    public void setDataSourceMapping(Map<String, DataSource> dataSourceMapping) {
        this.dataSourceMapping = dataSourceMapping;
    }

    public TenantConfigMapping getTenantConfigMapping() {
        return tenantConfigMapping;
    }


    public void setTenantConfigMapping(TenantConfigMapping tenantConfigMapping) {
        this.tenantConfigMapping = tenantConfigMapping;
    }

    @Override
    protected DataSource getTargetDataSource(String tenant) {

        TenantConfig config = getTenantConfig(tenant);
        if( null != config && null != dataSourceMapping){
            DataSource dataSource = dataSourceMapping.get(config.getDataSourceName());
            if( null != dataSource ){
                return dataSource;
            }
        }

        return super.getTargetDataSource();
    }

    @Override
    protected Connection bindTenant(Connection connection, String tenant) throws SQLException {

        TenantConfig config = getTenantConfig(tenant);

        if( null != config ){
            if(!StringUtils.isEmpty(config.getDatabaseName())){
                connection.prepareStatement("USE "+config.getDatabaseName()).execute();
            }
            if(!StringUtils.isEmpty(config.getCatalogName())){
                connection.setCatalog( config.getCatalogName() );
            }
            if(!StringUtils.isEmpty(config.getSchemaName())){
                connection.setSchema( config.getSchemaName() );
            }
        }

        return connection;
    }

    private TenantConfig getTenantConfig(String tenant){
       if( null ==  tenantConfigMapping ){
           return null;
       }

       return tenantConfigMapping.getConfig(tenant);
   }


}
