package com.project.shopapp.services;

import com.project.shopapp.models.Entities.Token;
import com.project.shopapp.models.Entities.User;

public interface ITokenService {
    void addToken(User user, String token, boolean isMobile);
    Token deleteToken(String token);
}
