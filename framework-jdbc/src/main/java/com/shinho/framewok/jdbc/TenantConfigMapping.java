package com.shinho.framewok.jdbc;

import java.util.Map;

public interface TenantConfigMapping {

    TenantConfig getConfig(String tenant);

    Map<String,TenantConfig> asMap();

}
