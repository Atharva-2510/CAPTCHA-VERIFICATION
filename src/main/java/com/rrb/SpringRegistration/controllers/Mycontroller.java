package com.rrb.SpringRegistration.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrb.SpringRegistration.entities.User;
import com.rrb.SpringRegistration.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@Controller
public class Mycontroller {
	
	@Autowired
	private UserService userService;
	
	@GetMapping("/register")
	public String openRegPage(Model model) 
	{
		model.addAttribute("user", new User());
		return "register";
	}
	
	@PostMapping("/register")
	public String submitRegForm(@ModelAttribute("user") User user, Model model) 
	{	
		boolean status = userService.registerUser(user);
		if (status) 
		{
			model.addAttribute("successMsg", "Registration Successful");
		}
		else 
		{
			model.addAttribute("failedMsg", "Registration Failed");
		}
		return "register";
	}
	
	// reCAPTCHA configuration
	private static final String RECAPTCHA_SITE_KEY = "Enter your RECAPTCHA_SITE_KEY";
	private static final String RECAPTCHA_SECRET_KEY = "Enter your RECAPTCHA_SECRET_KEY";
	private static final String RECAPTCHA_VERIFY_URL = "Enter your RECAPTCHA_VERIFY_URL";
	
	@GetMapping("/login")
	public String openLoginForm(Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("recaptchaSiteKey", RECAPTCHA_SITE_KEY);
		return "login";
	}
	
	@PostMapping("/login")
	public String submitLoginForm(
			@ModelAttribute("user") User user,
			@RequestParam("g-recaptcha-response") String recaptchaResponse,
			HttpServletRequest request,
			Model model)
	{
		// Verify reCAPTCHA
		boolean isRecaptchaValid = verifyRecaptcha(recaptchaResponse, request.getRemoteAddr());
		
		if (!isRecaptchaValid) {
			model.addAttribute("errorMsg", "Please verify that you are not a robot");
			model.addAttribute("recaptchaSiteKey", RECAPTCHA_SITE_KEY);
			return "login";
		}
		
		User validUser = userService.loginUser(user.getEmail(), user.getPassword());
		if (validUser != null) 
		{
			model.addAttribute("modelName", validUser.getName());
			return "profile";
		} 
		else 
		{
			model.addAttribute("errorMsg", "Invalid Credentials");
			model.addAttribute("recaptchaSiteKey", RECAPTCHA_SITE_KEY);
			return "login";
		}
	}
	
	/**
	 * Verifies the reCAPTCHA response with Google's API
	 * 
	 * @param recaptchaResponse The g-recaptcha-response from the form
	 * @param remoteIp The IP address of the user
	 * @return true if verification is successful, false otherwise
	 */
	private boolean verifyRecaptcha(String recaptchaResponse, String remoteIp) {
		if (recaptchaResponse == null || recaptchaResponse.isEmpty()) {
			return false;
		}
		
		try {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			
			MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
			map.add("secret", RECAPTCHA_SECRET_KEY);
			map.add("response", recaptchaResponse);
			map.add("remoteip", remoteIp);
			
			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
			
			String response = restTemplate.postForObject(RECAPTCHA_VERIFY_URL, request, String.class);
			
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(response);
			
			return jsonNode.get("success").asBoolean();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@GetMapping("/logout")
	public String logout(HttpServletRequest request) 
	{
		HttpSession session = request.getSession(false);
		
		if(session!=null)
		{
			session.invalidate();
		}
		
		return "redirect:/login";
	}

}