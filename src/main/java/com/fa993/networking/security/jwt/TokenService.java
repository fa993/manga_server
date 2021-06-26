package com.fa993.networking.security.jwt;

import java.util.Map;

public interface TokenService {

    public String permanent(Map<String, String> attributes);

    public String expiring(Map<String, String> attributes);

    public Map<String, String> untrusted(String token);

    public Map<String, String> verify(String token);

}
