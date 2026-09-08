package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class BcryptGenerate {

    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        System.out.println(passwordEncoder.encode("Customer@123"));
        System.out.println(passwordEncoder.encode("Staff@123"));
        System.out.println(passwordEncoder.encode("Admin@123"));

        System.out.println(
                "Default timezone: "
                        + ZoneId.systemDefault()
        );

        System.out.println(
                "Current time: "
                        + LocalDateTime.now()
        );

    }

}
