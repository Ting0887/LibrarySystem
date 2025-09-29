package controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import model.PasswordResetToken;
import model.User;
import repository.PasswordResetTokenRepository;
import repository.UserRepository;

@Controller
public class UserController {
	@Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    @Value("${server.servlet.context-path}")
    private String projectName;

    // 顯示登入頁
    @GetMapping("/signin")
    public String loginPage(Model model, User user) {
        return "login";
    }
    
    // 登入驗證
    @PostMapping("/doLogin")
    public String loginUser(@ModelAttribute("user") User user, Model model, HttpSession httpSession) {
    	Optional<User> optionalUser = userRepository.findByEmail(user.getEmail());
    	
    	if(optionalUser.isEmpty()) {
    		model.addAttribute("loginError", "帳號不存在");
    		model.addAttribute("user", new User());
    		return "login";
    	}
    	
    	User userdb = optionalUser.get();
    	if(!passwordEncoder.matches(user.getPassword(), userdb.getPassword())) {
    		model.addAttribute("loginError", "密碼錯誤");
    		model.addAttribute("user", user);
    		return "login";
    	}
    	
    	UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userdb.getEmail(), 
                        null,
                        List.of(new SimpleGrantedAuthority(userdb.getRole()))
                );
    	
    	SecurityContext context = SecurityContextHolder.createEmptyContext();
    	context.setAuthentication(authentication);
    	SecurityContextHolder.setContext(context);
    	httpSession.setAttribute("SPRING_SECURITY_CONTEXT", context);
    	
    	// 登入成功
    	httpSession.setAttribute("loggedInUser", userdb.getUserName());
    	return "redirect:/home";
    }

    // 顯示註冊頁
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // 註冊處理
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult, Model model) {
    	if(bindingResult.hasErrors()) {
    		return "register";
    	}
    	if(!user.getPassword().equals(user.getConfirmPassword())) {
    		model.addAttribute("passwordError", "兩次輸入的密碼不一致");
    		return "register";
    	}
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("error", "Email 已被使用");
            return "register";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER"); // 預設新註冊的帳號是一般使用者
        userRepository.save(user);
        return "redirect:/login";
    }
    
    @GetMapping("/forgotpassword")
    public String forgotPasswordForm() {
    	return "forgotPassword";
    }
    
    @PostMapping("/forgotpassword")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
    	Optional<User> userOptional = userRepository.findByEmail(email);
    	if(userOptional.isEmpty()) {
    		model.addAttribute("error", "此email沒有被註冊過");
    		return "forgotPassword";
    	}
    	User user = userOptional.get();
    	String token = UUID.randomUUID().toString();
    	
    	Optional<PasswordResetToken> existingToken = passwordResetTokenRepository.findByUser(user);
    	PasswordResetToken resetToken;
    	if (existingToken.isPresent()) {
    	    resetToken = existingToken.get();
    	    resetToken.setToken(UUID.randomUUID().toString());
    	    resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
    	} else {
    	    resetToken = new PasswordResetToken(UUID.randomUUID().toString(), user, LocalDateTime.now().plusMinutes(30));
    	}
        passwordResetTokenRepository.save(resetToken);

        String resetUrl = frontendUrl + projectName + "/resetpassword?token=" + resetToken.getToken();

        // 發信
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("密碼重設");
        message.setText("請點擊以下連結重設密碼:\n" + resetUrl);
        mailSender.send(message);
    	
    	model.addAttribute("message", "已寄送密碼重設信件，請檢查您的信箱。");
        return "forgotPassword";
    }
    
    @GetMapping("/resetpassword")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty() || tokenOpt.get().isExpired()) {
            model.addAttribute("error", "連結無效或已過期");
            return "resetPassword";
        }
        
        model.addAttribute("token", token);
        return "resetPassword";
    }

    // 處理 reset 密碼
    @PostMapping("/resetpassword")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String newPassword,
                                       @RequestParam("confirmpassword") String confirmPassword,
                                       Model model) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty() || tokenOpt.get().isExpired()) {
            model.addAttribute("error", "連結無效或已過期");
            return "redirect:/resetpassword?token=" + token;
        }
        
        if(!newPassword.equals(confirmPassword)) {
        	model.addAttribute("error", "密碼不匹配");
        	return "redirect:/resetpassword?token=" + token;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // token 失效
        passwordResetTokenRepository.delete(resetToken);

        model.addAttribute("user", new User());
        model.addAttribute("message", "密碼已更新，請重新登入。");
        return "redirect:/signin?resetSuccess=true";
    }

    // 登入成功後導向
    @GetMapping("/home")
    public String home(Model model, HttpSession session) {
    	String userName = (String) session.getAttribute("loggedInUser");
    	model.addAttribute("userName", userName);
        return "home";
    }
}
