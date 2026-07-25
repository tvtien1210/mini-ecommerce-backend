package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.auth.register.CreateRegisterRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.auth.register.RegisterDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.User;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterDTO register(CreateRegisterRequest request) {

        //check mail ton tai chua? neu chua nem exception
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exits"); //neu mail da ton tai, code se dung tai day nho throw new ex
        }

        //neu mail chua toi tai tao moi user

        User user = new User();

        user.setUsername(request.getUsername());

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setEmail(request.getEmail());

        user.setFullName(request.getFullName());

        User savedUser = userRepository.save(user);

        return new RegisterDTO(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()

        );


    }
}
