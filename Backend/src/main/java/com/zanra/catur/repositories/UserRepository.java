package com.zanra.catur.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zanra.catur.models.User;

public interface UserRepository extends JpaRepository<User, Long> {}
