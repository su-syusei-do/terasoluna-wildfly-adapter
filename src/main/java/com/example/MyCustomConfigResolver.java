package com.example;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.wildfly.security.http.oidc.OidcClientConfiguration;
import org.wildfly.security.http.oidc.OidcClientConfigurationBuilder;
import org.wildfly.security.http.oidc.OidcClientConfigurationResolver;
import org.wildfly.security.http.oidc.OidcHttpFacade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyCustomConfigResolver implements OidcClientConfigurationResolver {

    private final Map<String, OidcClientConfiguration> cache = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(MyCustomConfigResolver.class);

    @Override
    public OidcClientConfiguration resolve(OidcHttpFacade.Request request) {
        String path = request.getURI();

        logger.info("path:" + path);

        // URLに含まれる文字列によってテナント設定を切り替える
        String tenant = "sample1";
        if (path.contains("/SampleWebApp2")) {
            tenant = "sample2";
        }
        logger.info("tenant:" + tenant);

        OidcClientConfiguration clientConfiguration = cache.get(tenant);
        if (clientConfiguration == null) {
            InputStream is = getClass().getResourceAsStream("/oidc-" + tenant + ".json"); // config to use based on the tenant
            clientConfiguration = OidcClientConfigurationBuilder.build(is);
            cache.put(tenant, clientConfiguration);
        }
        return clientConfiguration;
    }
}