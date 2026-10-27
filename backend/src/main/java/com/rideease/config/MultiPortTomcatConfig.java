package com.rideease.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MultiPortTomcatConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Value("${port.driver:8081}")
    private int driverPort;

    @Value("${port.admin:8082}")
    private int adminPort;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        // Driver Portal Connector on Port 8081
        Connector driverConnector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        driverConnector.setPort(driverPort);
        factory.addAdditionalTomcatConnectors(driverConnector);

        // Admin Portal Connector on Port 8082
        Connector adminConnector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        adminConnector.setPort(adminPort);
        factory.addAdditionalTomcatConnectors(adminConnector);
    }
}
