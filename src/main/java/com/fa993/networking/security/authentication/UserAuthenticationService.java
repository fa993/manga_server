package com.fa993.networking.security.authentication;

import com.fa993.networking.accounts.entities.Account;

import java.util.Optional;

public interface UserAuthenticationService {

    public Optional<String> login(Account ac);

    public Optional<Account> findAccount(String token);

    public void logout(Account ac);

}
