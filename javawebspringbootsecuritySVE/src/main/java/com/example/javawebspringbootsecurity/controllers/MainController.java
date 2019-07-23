package com.example.javawebspringbootsecurity.controllers;

import com.example.javawebspringbootsecurity.entities.User;
import com.example.javawebspringbootsecurity.repositories.UserRepository;
import com.example.javawebspringbootsecurity.security.TokenUtils;
import com.example.javawebspringbootsecurity.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private TokenUtils tokenUtils;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager; //njoj dajemo token, dace nam autentifikaciju da vidi da li je dobar par i dal moze da radi nesto, da se uloguje i ostalo

    @RequestMapping (value = "/login", method = RequestMethod.GET)
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {
        System.out.println(username + " " + password);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new ResponseEntity<>(tokenUtils.generateToken(userDetails), HttpStatus.OK);
    }
    @PreAuthorize(value = "hasRole('User')")
    @RequestMapping (value = "/bad_login")
    public ResponseEntity<String> no() {
        return new ResponseEntity<>("Los loginnn", HttpStatus.OK);
    }

    @RequestMapping (value = "/index")
    public ResponseEntity<String> yes() {
        return new ResponseEntity<>("Dobrodosli", HttpStatus.OK);
    }
    @RequestMapping (value = "/register")
    public ResponseEntity<String> register(@RequestParam String username, @RequestParam String password, @RequestParam String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);

        userRepository.save(user);

        return new ResponseEntity<>("Uspesna registracija!", HttpStatus.OK);
    }
}
