package com.shinho.framewok.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractMultiTenantDataSourceProxy extends LazyConnectionDataSourceProxy {

    /**
     * Create a new DelegatingDataSource.
     *
     * @see #setTargetDataSource
     */
    public AbstractMultiTenantDataSourceProxy() {
    }

    /**
     * Create a new DelegatingDataSource.
     *
     * @param targetDataSource the target DataSource
     */
    public AbstractMultiTenantDataSourceProxy(DataSource targetDataSource) {
        super(targetDataSource);
    }

    @Autowired(required = false)
    private TenantAware tenantAware;

    public TenantAware getTenantAware() {
        return tenantAware;
    }

    public void setTenantAware(TenantAware tenantAware) {
        this.tenantAware = tenantAware;
    }

    @Override
    public Connection getConnection() throws SQLException {

        String tenant = tenant();
        Connection connection = getTargetDataSource(tenant).getConnection();
        return tryBindTenant(connection,tenant);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {

        String tenant = tenant();
        Connection connection = getTargetDataSource(tenant).getConnection(username, password);
        return tryBindTenant(connection,tenant);
    }

    protected String tenant(){
        if( null != tenantAware ){
            return tenantAware.currentTenant();
        }
        return null;
    }

    private Connection tryBindTenant(Connection connection,String tenant) throws SQLException {
        try {
            return bindTenant(connection,tenant);
        } catch (Throwable e) {
            connection.close();
            throw e;
        }
    }

    protected DataSource getTargetDataSource(String tenant) {
        return super.getTargetDataSource();
    }

    protected abstract Connection bindTenant(Connection connection,String tenant) throws SQLException ;
}
