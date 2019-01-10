package com.shinho.framewok.jdbc;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class SimpleMultiTenantDataSourceProxy extends AbstractMultiTenantDataSourceProxy {

    private MultiTenantStrategy multiTenantStrategy = MultiTenantStrategy.NONE;


    public SimpleMultiTenantDataSourceProxy() {

    }

    public SimpleMultiTenantDataSourceProxy(DataSource targetDataSource) {
        super(targetDataSource);
    }

    public SimpleMultiTenantDataSourceProxy(DataSource targetDataSource,MultiTenantStrategy multiTenantStrategy) {
        super(targetDataSource);
        this.multiTenantStrategy = multiTenantStrategy;
    }


    public MultiTenantStrategy getMultiTenantStrategy() {
        return multiTenantStrategy;
    }

    public void setMultiTenantStrategy(MultiTenantStrategy multiTenantStrategy) {
        this.multiTenantStrategy = multiTenantStrategy;
    }


    @Override
    protected Connection bindTenant(Connection connection, String tenant) throws SQLException {

        if( null != tenant ){
            switch ( multiTenantStrategy ){
                case SCHEMA:connection.setSchema( tenant );break;
                case CATALOG:connection.setCatalog( tenant );break;
                case DATABASE:connection.prepareStatement("USE "+tenant).execute();break;
                case NONE:break;
                default:break;
            }
        }

        return connection;
    }


}
