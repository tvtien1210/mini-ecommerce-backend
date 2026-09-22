package com.chantaro.ecommerce.mini_ecommerce_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling //Hãy cho phép các method có @Scheduled chạy tự động theo thời gian.
@SpringBootApplication
public class MiniEcommerceBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiniEcommerceBackendApplication.class, args);
	}

}
