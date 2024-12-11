package registration;

import domain.Profile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public class RegistrationDTO {

    @NotBlank(message = "Username is required.")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters.")
    private String username;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    private String password;

    /*@NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format.")
    private String email;*/

    public RegistrationDTO() {}

    public RegistrationDTO(Profile userProfile) {
        this.username = userProfile.getUsername();
        this.password = userProfile.getPassword();
      /*  this.email = userProfile.getEmail();*/
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

 /*   public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "RegistrationDTO{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }*/
}
