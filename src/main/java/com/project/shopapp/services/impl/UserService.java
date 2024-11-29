package com.project.shopapp.services.impl;

import com.project.shopapp.DTO.UserDTO;
import com.project.shopapp.components.JWTTokenUtil;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.PermissionDenyException;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.services.IUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

@Service
public class UserService implements IUserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JWTTokenUtil jwtTokenUtil;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public User createUser(UserDTO userDTO) throws Exception {
        String phoneNumber = userDTO.getPhoneNumber();
        if(userRepository.existsByPhoneNumber(phoneNumber) || phoneNumber.length() != 10){
            throw new DataIntegrityViolationException("Phone number isn't suitable");
        }
        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        if(role.getName().toUpperCase().equals(Role.ADMIN)) {
            throw new PermissionDenyException("You cannot register as an admin account");
        }
        User user = User.builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(phoneNumber)
                .password(userDTO.getPassword())
                .address(userDTO.getAddress())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId(userDTO.getFacebookAccountId())
                .googleAccountId(userDTO.getGoogleAccountId())
                .active(true)
                .build();
        user.setRole(role);

        if (userDTO.getFacebookAccountId() == 0 && userDTO.getGoogleAccountId() == 0) {
            String password = userDTO.getPassword();
           String encodedPassword = passwordEncoder.encode(password);
            user.setPassword(encodedPassword);
        }
        return userRepository.save(user);
    }

    @Override
    public String login(String phoneNumber, String password) throws Exception {
        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);
        if(user.isEmpty()){
            throw new DataIntegrityViolationException("Phone number not found");
        }
        User existingUser = user.get();
        if(existingUser.getFacebookAccountId() == 0 && existingUser.getGoogleAccountId() == 0){
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
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(phoneNumber, password, existingUser.getAuthorities()));
        return jwtTokenUtil.generateToken(existingUser);
    }

    @Override
    public User getUserDetailFromToken(String extraStringToken) throws Exception {
        if(jwtTokenUtil.isTokenExpired(extraStringToken)){
            throw new Exception("Expired or invalid token");
        }
        String phoneNumber = jwtTokenUtil.extractPhoneNumber(extraStringToken);
        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);

        if(user.isPresent()){
            return user.get();
        }
        else{
            throw new Exception("Invalid token");
        }
    }

    @Transactional
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
