package controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import model.User;
import repository.UserRepository;

@Controller
public class UserController {
	@Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    // 登入成功後導向
    @GetMapping("/home")
    public String home(Model model, HttpSession session) {
    	String userName = (String) session.getAttribute("loggedInUser");
    	model.addAttribute("userName", userName);
        return "home";
    }
}
