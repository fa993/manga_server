package com.fa993.networking.controllers;

import com.fa993.networking.Constants;
import com.fa993.networking.accounts.repository.AccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/secure")
public class SecureController {

    @Autowired
    AccountManager repo;

    @GetMapping("/recommendations")
    public String recommend() {
        return "Hello";
    }

    @DeleteMapping("/delete")
    public String deleteSelf(@RequestAttribute(value = Constants.REQUESTER_ID) String accountId){
        repo.deleteAccount(accountId);
        return Boolean.TRUE.toString();
    }


}
