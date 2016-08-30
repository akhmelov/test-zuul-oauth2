package com.example.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by akhmelov on 8/21/16.
 */
@RestController
public class IndexController {

//    @PreAuthorize("#oauth2.hasScope('ui')")
    @RequestMapping({"/user", "/me"})
    public Map<String, String> user(Authentication authentication) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("name", authentication.getName());
        map.put("role", "test3");

        return map;
    }

    @RequestMapping("/test")
    public String test(){
        return "test_test_5";
    }
}
