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
	@NotBlank(message = "確認密碼不能為空")
	private String confirmPassword;
	private String role;
	public Long getId() { return id; } 
	public void setId(Long id) { this.id = id; } 
	public String getUserName() { return userName; } 
	public void setUserName(String userName) { this.userName = userName; } 
	public String getEmail() { return email; } 
	public void setEmail(String email) { this.email = email; } 
	public String getPassword() { return password; } 
	public void setPassword(String password) { this.password = password; } 
	public String getConfirmPassword() {return confirmPassword;}
	public void setConfirmPassword(String confirmPassword) {this.confirmPassword = confirmPassword;}
	public String getRole() { return role; }
	public void setRole(String role) { this.role = role; }
}
