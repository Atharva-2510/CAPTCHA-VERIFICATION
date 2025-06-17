package com.rrb.SpringRegistration.services;

import com.rrb.SpringRegistration.entities.User;
import com.rrb.SpringRegistration.repositories.UserRepository;

public interface UserService{
	public boolean registerUser(User user);
	public User loginUser(String email, String password);
}
