package com.learning.roles.service;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    public static final int MAX_NUMBER_OF_ATTEMPTS = 2;
    public static final int ATTEMPTS_INCREMENT = 1;
    private LoadingCache<String, Integer> loginAttemptsCache;

    public LoginAttemptService(){
        super();
        this.loginAttemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(100)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) throws Exception {
                        return 0;
                    }
                });
    }

    public void evictUserFromLoginAttemptCache(String username){
        this.loginAttemptsCache.invalidate(username);
    }

    public void addUserToLoginAttemptCache(String username){
        int attempts = 0;
        try{
            attempts = ATTEMPTS_INCREMENT + this.loginAttemptsCache.get(username);
            this.loginAttemptsCache.put(username, attempts);
        }
        catch (Exception e){

        }
    }

    public boolean hasExceededMaxAttempts(String username) {
        try {
            return this.loginAttemptsCache.get(username) > MAX_NUMBER_OF_ATTEMPTS;
        }
        catch (ExecutionException e){
            return false;
        }
    }




}
