package com.learning.roles;

import com.learning.roles.constant.FileConstant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;

@SpringBootApplication
public class RolesApplication {

	public static void main(String[] args) {
		SpringApplication.run(RolesApplication.class, args);
		new File(FileConstant.USER_FOLDER).mkdirs();
	}


	@Bean
	public BCryptPasswordEncoder CryptPasswordEncoder() throws Exception {
		return new BCryptPasswordEncoder();
	}
}
