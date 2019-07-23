package com.example.javawebspringbootsecurity.repositories;

import com.example.javawebspringbootsecurity.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
