package com.example.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.Collection;

/**
 * Created by akhmelov on 8/21/16.
 */
@Controller
public class IndexController {

    Logger logger = Logger.getLogger(IndexController.class);

    @Autowired
    private OAuth2RestTemplate oAuth2RestTemplate;

    @RequestMapping("/")
    public String home(Authentication authentication){

        if (authentication != null) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            Object credentials = authentication.getCredentials();
            logger.debug(authorities);
        }
        String name = authentication.getName();
        Object credentials = authentication.getCredentials();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        return "index";
    }


    @RequestMapping("/test")
    public String test(){
        String tmp = oAuth2RestTemplate.getForObject("http://pk:8083/uaa/test", String.class);
        return tmp;
    }
}
