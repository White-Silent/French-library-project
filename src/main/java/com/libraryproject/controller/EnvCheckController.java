package com.libraryproject.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class EnvCheckController {

    @GetMapping("/check-env")
    public Map<String, String> checkEnv() {
        Map<String, String> env = new HashMap<>();
        env.put("MYSQL_HOST", System.getenv("MYSQL_HOST"));
        env.put("MYSQL_PORT", System.getenv("MYSQL_PORT"));
        env.put("MYSQL_USER", System.getenv("MYSQL_USER"));
        env.put("MYSQL_PASSWORD", System.getenv("MYSQL_PASSWORD"));
        env.put("MYSQL_DATABASE", System.getenv("MYSQL_DATABASE"));
        return env;
    }
}
