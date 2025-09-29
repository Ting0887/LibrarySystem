package net.javaguides.sms.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig{
	
	@Autowired
    private CustomUserDetailsService userDetailsService;

	@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 密碼加密
    }
	
	@Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                   .userDetailsService(userDetailsService)
                   .passwordEncoder(passwordEncoder())
                   .and()
                   .build();
    }
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception{
		 httpSecurity
         .authorizeHttpRequests(auth -> auth
             .requestMatchers("/register", "/signin", "/login", "/doLogin", 
            		 "/resetpassword", "/forgotpassword", "/css/**").permitAll()
             .requestMatchers("/admin/**").hasRole("ADMIN")
             .requestMatchers("/user/**").hasRole("USER")
             .anyRequest().authenticated()
         )
         .formLogin(form -> form.disable()) // 禁用 Spring Security 自帶 formLogin
         .logout(logout -> logout
             .logoutUrl("/logout")
             .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
             .logoutSuccessUrl("/signin?logout")
             .permitAll()
         );

     return httpSecurity.build();
	}
}
