package model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "passwordResetToken")
public class PasswordResetToken {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@OneToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	private LocalDateTime expiryDate;
	
	@Column(unique = true, nullable = false)
    private String token;
	
	public PasswordResetToken() {}

	public PasswordResetToken(String token, User user, LocalDateTime expiryDate) {
		this.token = token;
		this.user = user;
		this.expiryDate = expiryDate;
	}
	
	public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
	
}
