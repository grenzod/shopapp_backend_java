package com.project.shopapp.controllers;

import com.project.shopapp.DTO.UserDTO;
import com.project.shopapp.DTO.UserLoginDTO;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.responses.LoginResponse;
import com.project.shopapp.responses.ResponseObject;
import com.project.shopapp.responses.UserResponse;
import com.project.shopapp.services.IAuthorService;
import com.project.shopapp.services.IUserService;
import com.project.shopapp.components.LocalizationUtil;
import com.project.shopapp.services.impl.UserService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService iUserService;
    private final UserService userService;
    private final IAuthorService iAuthorService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> addUser(@Valid @RequestBody UserDTO userDTO,
                                     BindingResult result) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }

            if(userDTO.getEmail() == null || userDTO.getEmail().trim().isBlank()) {
                if(userDTO.getPhoneNumber() == null || userDTO.getPhoneNumber().isBlank()) {
                    return ResponseEntity.badRequest().body(ResponseObject.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .data(null)
                            .message("Required field email or phone number")
                            .build());
                }
            }

            if(!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
                return ResponseEntity.badRequest().body(MessageKeys.MATCH_FAILED);
            }
            User user = iUserService.createUser(userDTO);
            return ResponseEntity.ok().body(user);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> logUser(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        try {
            String token = iUserService.login(userLoginDTO);

            return ResponseEntity.ok().body(LoginResponse.builder()
                            .message(LocalizationUtil.getLocaleMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                            .token(token)
                    .build());
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(LoginResponse.builder()
                            .message(LocalizationUtil.getLocaleMessage(MessageKeys.LOGIN_FAILED, e.getMessage()))
                    .build());
        }
    }

    @GetMapping("/auth/social-login")
    public ResponseEntity<String> socialLogin(@RequestParam("login_type") String loginType,
                                              HttpServletRequest request) {
        loginType = loginType.trim().toLowerCase();
        String url = iAuthorService.generateAuthor(loginType);
        return ResponseEntity.ok().body(url);
    }

    @GetMapping("/auth/social/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code,
                                      @RequestParam("login_type") String loginType,
                                      HttpServletRequest request) throws Exception{
        Map<String, Object> userInfo = iAuthorService.authenticateAndFetchProfile(code, loginType);

        if(userInfo == null) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject("Fail to authenticate", HttpStatus.BAD_REQUEST, null));
        }

        String accountId = "";
        String name = "";
        String email = "";

        if(loginType.trim().equals("google")){
            accountId = userInfo.get("sub").toString();
            name = userInfo.get("name").toString();
            email = userInfo.get("email").toString();
        }

        UserLoginDTO userLoginDTO = UserLoginDTO.builder()
                .email(email)
                .fullName(name)
                .password("")
                .phoneNumber("")
                .build();

        if(loginType.trim().equals("google")){
            userLoginDTO.setGoogle_account_id(accountId);
            userLoginDTO.setFacebook_account_id("");
        }
        else{
            userLoginDTO.setFacebook_account_id(accountId);
            userLoginDTO.setGoogle_account_id("");
        }

        return this.logUser(userLoginDTO);
    }

    @PostMapping("/details")
    public ResponseEntity<UserResponse> getUserDetails(@RequestHeader("Authorization") String authorization) {
        try {
            String extraStringToken = authorization.substring(7);
            User user = userService.getUserDetailFromToken(extraStringToken);
            return ResponseEntity.ok(UserResponse.fromUser(user));
        }
        catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/updates/{userId}")
    public ResponseEntity<?> updateUser(@RequestBody UserDTO userDTO,
                                        @PathVariable Long userId,
                                        @RequestHeader("Authorization") String authorization){
        try {
            String extraStringToken = authorization.substring(7);
            User user = userService.getUserDetailFromToken(extraStringToken);
            if(!Objects.equals(user.getId(), userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            User userUpdate = userService.updateUser(userDTO, userId);
            return ResponseEntity.ok().body(UserResponse.fromUser(userUpdate));
        }
        catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
}
