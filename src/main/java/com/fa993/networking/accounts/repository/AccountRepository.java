package com.fa993.networking.accounts.repository;

import com.fa993.networking.accounts.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {

     public Account findByEmailAndPassword(String username, String password);
     public Account findByEmail(String email);
}
