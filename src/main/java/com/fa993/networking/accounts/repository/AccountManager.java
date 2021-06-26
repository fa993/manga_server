package com.fa993.networking.accounts.repository;

import com.fa993.networking.accounts.entities.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AccountManager {

    @Autowired
    private AccountRepository repo;

    public void addUser(Account ac){
        ac.setId(UUID.randomUUID().toString());
        repo.save(ac);
    }

    public boolean login(Account ac){
        return repo.findByEmailAndPassword(ac.getUsername(), ac.getPassword()) != null;
    }

    public Optional<Account> getAccount(String email){
        return Optional.ofNullable(repo.findByEmail(email));
    }

    public void deleteAccount(String id){
        repo.deleteById(id);
    }

}
