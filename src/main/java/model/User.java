package model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	private Long id;
	
	private String userName; 
	
	@Email(message = "Email 格式不正確")
	private String email;
	
	@NotBlank(message = "密碼不能為空")
	@Size(min = 6, message = "密碼至少為6碼")
	private String password;
	
	@Transient
	private String confirmPassword;
	
	private String role;
}
