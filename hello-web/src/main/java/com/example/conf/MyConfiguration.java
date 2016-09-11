package com.example.conf;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.buf.MessageBytes;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * Created by akhmelov on 8/23/16.
 */
@Configuration
public class MyConfiguration {

    Logger logger = Logger.getLogger(MyConfiguration.class);

//    @Bean
//    public AuthoritiesExtractor authoritiesExtractor() {
//        return map -> {
//            final List<GrantedAuthority> list = AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER,ROLE_GREATE");
//            return list;
//        };
//    }

    @Bean
    public FilterRegistrationBean oauth2ClientFilterRegistration(
            OAuth2ClientContextFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }

    @Bean
    public OAuth2RestTemplate getRestTemplate(OAuth2ProtectedResourceDetails resource, OAuth2ClientContext context){
        return new OAuth2RestTemplate(resource, context);
    }

    @Bean
    public EmbeddedServletContainerCustomizer proxyRedirectionFilter() {
        return (final ConfigurableEmbeddedServletContainer container) -> {

            final TomcatEmbeddedServletContainerFactory tomcat = (TomcatEmbeddedServletContainerFactory) container;

            tomcat.addContextCustomizers((final Context context) -> {
                context.setMapperContextRootRedirectEnabled(false);
            });

            tomcat.addContextValves(new ValveBase() {

                @Override
                public void invoke(final Request request, final Response response) throws IOException, ServletException {

                    final MessageBytes serverNameMB = request.getCoyoteRequest().serverName();
                    Enumeration<String> headerNames = request.getHeaderNames();
                    logger.debug(headerNames);

                    String originalServerName = null;
                    final String forwardedHost = request.getHeader("x-forwarded-host");
                    if (forwardedHost != null) {
                        originalServerName = serverNameMB.getString();
                        serverNameMB.setString(forwardedHost);
                    }

                    int originalPort = -1;
                    final String forwardedPort = request.getHeader("x-forwarded-port");
                    if (forwardedPort != null) {
                        try {
                            originalPort = request.getServerPort();
                            request.setServerPort(Integer.valueOf(forwardedPort));
                        } catch (final NumberFormatException e) {
                            logger.debug("ignoring forwarded port {}" + forwardedPort);
                        }
                    }

                    try {
                        getNext().invoke(request, response);
                    } finally {
                        if (forwardedHost != null) {
                            serverNameMB.setString(originalServerName);
                        }
                        if (forwardedPort != null) {
                            request.setServerPort(originalPort);
                        }
                    }
                }
            });
        };
    }

}
