package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptGenerate {

    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        System.out.println(passwordEncoder.encode("Customer@123"));
        System.out.println(passwordEncoder.encode("Staff@123"));
        System.out.println(passwordEncoder.encode("Admin@123"));



    }

}
