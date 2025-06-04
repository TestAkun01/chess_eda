package com.zanra.catur.controllers;

import com.zanra.catur.dto.AuthDTO;
import com.zanra.catur.dto.ResponseDTO;
import com.zanra.catur.events.UserLoggedInEvent;
import com.zanra.catur.events.UserLoggedOutEvent;
import com.zanra.catur.events.UserRegisteredEvent;
import com.zanra.catur.kafka.producers.AuthEventProducer;
import com.zanra.catur.models.User;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthEventProducer authEventProducer;

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody AuthDTO.Request request) {
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ResponseDTO.error(HttpStatus.BAD_REQUEST, "Username is required"));
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ResponseDTO.error(HttpStatus.BAD_REQUEST, "Password is required"));
        }
        UserRegisteredEvent event = new UserRegisteredEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setUsername(request.getUsername());
        event.setPassword(request.getPassword());
        authEventProducer.sendUserRegisteredEvent(event);
        return ResponseEntity
                .ok(new AuthDTO.Response("Login initiated, please wait for authentication", event.getEventId()));
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody AuthDTO.Request request, HttpServletResponse response) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(ResponseDTO.error(HttpStatus.BAD_REQUEST, "Password is required"));
        }

        UserLoggedInEvent event = new UserLoggedInEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setUsername(request.getUsername());
        event.setPassword(request.getPassword());
        authEventProducer.sendUserLoggedInEvent(event);
        return ResponseEntity
                .ok(new AuthDTO.Response("Login initiated, please wait for authentication", event.getEventId()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(HttpServletResponse response) {
        UserLoggedOutEvent event = new UserLoggedOutEvent();
        authEventProducer.sendUserLoggedOutEvent(event);

        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok(ResponseDTO.success("Logout initiated"));
    }

    @GetMapping("/me")
    public ResponseEntity<Object> me(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseDTO.error(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        }

        AuthDTO.Response res = new AuthDTO.Response();
        res.setUserId(user.getId());
        res.setUsername(user.getUsername());

        return ResponseEntity.ok(res);
    }
}