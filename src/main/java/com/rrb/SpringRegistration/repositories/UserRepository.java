package com.rrb.SpringRegistration.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rrb.SpringRegistration.entities.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	User findByEmail(String email);
}
