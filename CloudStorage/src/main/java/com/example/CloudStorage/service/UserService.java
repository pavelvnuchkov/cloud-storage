package com.example.CloudStorage.service;

import com.example.CloudStorage.Exception.DataException;
import com.example.CloudStorage.config.jwt.JwtService;
import com.example.CloudStorage.dto.ExceptionDto;
import com.example.CloudStorage.dto.TokenDto;
import com.example.CloudStorage.dto.UserDto;
import com.example.CloudStorage.model.Token;
import com.example.CloudStorage.model.User;
import com.example.CloudStorage.repository.TokenRepository;
import com.example.CloudStorage.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repository;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;


    public UserService(UserRepository repository, JwtService jwtService, TokenRepository tokenRepository) {
        this.repository = repository;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
    }

    public TokenDto auth(UserDto userDto) {
        User user = getUser(userDto);
        return jwtService.jwtGenerateToken(user.getLogin());

    }

    public boolean blockedToken(String token) {
        tokenRepository.save(new Token(jwtService.getId(token), token));
        return true;
    }

    private User getUser(UserDto userDto) {
        Optional<User> user = repository.findByLogin(userDto.getLogin());
        if (user.isPresent()) {
            if (user.get().getPassword().equals(userDto.getPassword())) {
                return user.get();
            }
        }
        throw new DataException(new ExceptionDto("Error input data", 0000));
    }
}
