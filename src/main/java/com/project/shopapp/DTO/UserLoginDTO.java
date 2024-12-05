package com.project.shopapp.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginDTO {
    @JsonProperty("phone_number")
//    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @JsonProperty("email")
    private String email;

    @JsonProperty("full_name")
    private String fullName;

    @NotBlank(message = "Password can not be blank")
    private String password;

    private String google_account_id;

    private String facebook_account_id;

//    @JsonProperty("role_id")
//    @Min(value = 1, message = "You must enter role's id")
//    private Long roleId;
}
