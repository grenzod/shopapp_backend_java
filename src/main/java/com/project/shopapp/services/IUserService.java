package com.project.shopapp.services;

import com.project.shopapp.DTO.UserDTO;
import com.project.shopapp.DTO.UserLoginDTO;
import com.project.shopapp.models.User;
import com.project.shopapp.responses.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IUserService {
    User createUser(UserDTO userDTO) throws Exception;
    String login(UserLoginDTO userLoginDTO) throws Exception;
    User getUserDetailFromToken(String extraStringToken) throws Exception;
    User updateUser(UserDTO userDTO,Long userId) throws Exception;
    User deleteUser(Long userId) throws Exception;
    Page<UserResponse> getAllUsers(String keyword, PageRequest pageRequest);
}
