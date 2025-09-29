package net.javaguides.sms.config;

import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import model.User;
import repository.UserRepository;

@Configuration
public class DataInitializer {
	@Bean
    CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            Optional<User> adminOptional = userRepository.findByEmail("admin@example.com");
            if (adminOptional.isEmpty()) {
                User admin = new User();
                admin.setUserName("admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setConfirmPassword(passwordEncoder.encode("123456"));
                admin.setRole("ROLE_ADMIN");
                userRepository.save(admin);
                System.out.println("Admin 帳號已建立: admin@example.com / 123456");
            } else {
                System.out.println("Admin 帳號已存在");
            }
        };
    }
}
