package com.project.shopapp.services.impl;

import com.project.shopapp.DTO.UserDTO;
import com.project.shopapp.DTO.UserLoginDTO;
import com.project.shopapp.components.JWTTokenUtil;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.PermissionDenyException;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.services.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public User createUser(UserDTO userDTO) throws Exception {
        String phoneNumber = userDTO.getPhoneNumber();
        String email = userDTO.getEmail();
        if(!phoneNumber.isBlank() && userRepository.existsByPhoneNumber(phoneNumber)){
            throw new DataIntegrityViolationException("Phone already in use");
        }
        if(!email.isBlank() && userRepository.existsByEmail(email)){
            throw new DataIntegrityViolationException("Email already in use");
        }
        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        if(role.getName().toUpperCase().equals(Role.ADMIN)) {
            throw new PermissionDenyException("You cannot register as an admin account");
        }
        User user = User.builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(phoneNumber)
                .email(email)
                .password(userDTO.getPassword())
                .address(userDTO.getAddress())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId(userDTO.getFacebookAccountId())
                .googleAccountId(userDTO.getGoogleAccountId())
                .active(true)
                .build();
        user.setRole(role);

        if (Objects.equals(userDTO.getFacebookAccountId(), "")
                && Objects.equals(userDTO.getGoogleAccountId(), "")) {
            String password = userDTO.getPassword();
            String encodedPassword = passwordEncoder.encode(password);
            user.setPassword(encodedPassword);
        }
        return userRepository.save(user);
    }

    @Override
    public String login(UserLoginDTO userLoginDTO) throws Exception {
        String phoneNumber = userLoginDTO.getPhoneNumber();
        String password = userLoginDTO.getPassword();
        String email = userLoginDTO.getEmail();

        Optional<User> user = Optional.empty();
        String subject = null;

        if (userLoginDTO.getGoogle_account_id() != null) {
            user = userRepository.findByGoogleAccountId(userLoginDTO.getGoogle_account_id());
            subject = email;

            if (user.isEmpty()) {
                User newUser = User.builder()
                        .fullName(userLoginDTO.getFullName())
                        .phoneNumber(phoneNumber)
                        .email(email)
                        .password(userLoginDTO.getPassword())
                        .facebookAccountId(userLoginDTO.getFacebook_account_id())
                        .googleAccountId(userLoginDTO.getGoogle_account_id())
                        .role(roleRepository.findById(1L).orElse(null))
                        .active(true)
                        .build();
                newUser = userRepository.save(newUser);
                user = Optional.of(newUser);
            }

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(subject, null, user.get().getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return jwtTokenUtil.generateToken(user.get());
        }


        if(phoneNumber != null && userRepository.existsByPhoneNumber(phoneNumber)) {
            user = userRepository.findByPhoneNumber(phoneNumber);
            subject = phoneNumber;
        }

        if(user.isEmpty() && email != null) {
            user = userRepository.findByEmail(email);
            subject = email;
        }

        if(user.isEmpty()){
            throw new DataIntegrityViolationException("Your information is incorrect");
        }

        User existingUser = user.get();
        if(Objects.equals(existingUser.getFacebookAccountId(), "")
                && Objects.equals(existingUser.getGoogleAccountId(), "")){
            if(!passwordEncoder.matches(password, existingUser.getPassword())){
                throw new BadCredentialsException("Wrong password or phone number !!");
            }
        }

        if(existingUser.getRole() == null){
            throw new DataNotFoundException("Role not found");
        }
        if(!user.get().isActive()){
            throw new DataNotFoundException("This account is not active");
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(subject,
                        password,
                        existingUser.getAuthorities()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtTokenUtil.generateToken(existingUser);
    }

    @Override
    public User getUserDetailFromToken(String extraStringToken) throws Exception {
        if(jwtTokenUtil.isTokenExpired(extraStringToken)){
            throw new Exception("Expired or invalid token");
        }
        String phoneNumber = jwtTokenUtil.extractPhoneNumber(extraStringToken);
        String email = jwtTokenUtil.extractEmail(extraStringToken);
        Optional<User> user = Optional.empty();
        if(!Objects.equals(phoneNumber, ""))  user = userRepository.findByPhoneNumber(phoneNumber);
        else user = userRepository.findByEmail(email);

        if(user.isPresent()){
            return user.get();
        }
        else{
            throw new Exception("Invalid token");
        }
    }

    @Override
    public User updateUser(UserDTO userDTO,Long userId) throws Exception {
        User existingUser =
                userRepository.findById(userId).orElseThrow(() -> new DataNotFoundException("Not found User"));

        if(userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()){
            String newPassword = userDTO.getPassword();
            newPassword = passwordEncoder.encode(newPassword);
            existingUser.setPassword(newPassword);
        }

        return userRepository.save(existingUser);
    }
}
