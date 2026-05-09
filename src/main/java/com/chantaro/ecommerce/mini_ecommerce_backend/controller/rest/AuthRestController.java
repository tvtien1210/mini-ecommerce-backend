package com.chantaro.ecommerce.mini_ecommerce_backend.controller.rest;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.login.LoginRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.security.service.CustomUserDetailsService;
import com.chantaro.ecommerce.mini_ecommerce_backend.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {
    // Dung de check login
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    public AuthRestController(AuthenticationManager authenticationManager, JwtService jwtService, CustomUserDetailsService customUserDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest rq) {

        //Buoc 1: Kiem tra username, password co dung khong?
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        rq.getUsername(),
                        rq.getPassword()));
        //Neu sai -> auto throw exception

        //Buoc 2: load user tu database
        UserDetails user = customUserDetailsService.loadUserByUsername(rq.getUsername());

        //Buoc 3: tao token

        return Map.of("accessToken", jwtService.generateAccessToken(user),
                "refreshToken", jwtService.generateRefreshToken(user));


    }


}

/*security/
  JwtService.java
  JwtFilter.java
  SecurityConfig.java
  CustomUserDetailsService.java

controller/
  AuthController.java

dto/
  LoginRequest.java*/
