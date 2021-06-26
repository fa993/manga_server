package com.fa993.networking.security.jwt;

import com.fa993.networking.accounts.entities.Account;
import com.fa993.networking.accounts.repository.AccountManager;
import com.fa993.networking.security.authentication.UserAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TokenAuthenticationService implements UserAuthenticationService {

    @Autowired
    private TokenService service;

    @Autowired
    private AccountManager repo;

    @Override
    public Optional<String> login(Account ac){
        if(repo.login(ac)) {
            Map<String, String> m = new HashMap<>();
            m.put("username", ac.getUsername());
            return Optional.of(service.expiring(m));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Account> findAccount(String token){
        return Optional.of(service.verify(token)).map(t-> t.get("username")).flatMap(t-> repo.getAccount(t));
    }

    @Override
    public void logout(Account ac) {

    }

}

