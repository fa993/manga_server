package com.fa993.networking.controllers;

import com.fa993.networking.accounts.entities.Account;
import com.fa993.networking.accounts.repository.AccountManager;
import com.fa993.networking.security.authentication.IncorrectCredentialsException;
import com.fa993.networking.security.authentication.UserAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    UserAuthenticationService service;

    @Autowired
    AccountManager repo;

    @PostMapping("/login")
    public String login(@RequestBody Account ac){
        return service.login(ac).orElseThrow(() -> new IncorrectCredentialsException());
    }

    @PutMapping("/register")
    public String addUser(@RequestBody Account ac) {
        repo.addUser(ac);
        return login(ac);
    }

    @ExceptionHandler(IncorrectCredentialsException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    public String exception() {
        return "Incorrect Credentials";
    }

}
