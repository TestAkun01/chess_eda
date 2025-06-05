package com.zanra.catur.services;

import com.zanra.catur.models.User;
import com.zanra.catur.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public User register(String username, String rawPassword) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
        user.setRating(1200);
        user.setStatus(User.UserStatus.FREE);
        return userRepository.save(user);
    }

    public Optional<User> authenticate(String username, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty())
            return Optional.empty();

        User user = userOpt.get();
        if (!BCrypt.checkpw(rawPassword, user.getPasswordHash()))
            return Optional.empty();

        return Optional.of(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public void updateStatus(User user, User.UserStatus status) {
        user.setStatus(status);
        userRepository.save(user);
    }

    public void setPlayingStatus(User user, Long gameId) {
        user.setGameId(gameId);
        user.setStatus(User.UserStatus.IN_GAME);
        userRepository.save(user);
    }

    public void resetStatus(User user) {
        user.resetStatus();
        userRepository.save(user);
    }

}
