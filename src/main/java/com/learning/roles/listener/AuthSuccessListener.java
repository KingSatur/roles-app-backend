package com.learning.roles.listener;


import com.learning.roles.service.LoginAttemptService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthSuccessListener {

    private final LoginAttemptService loginAttemptService;

    public AuthSuccessListener(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }


    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event){
        Object principal = event.getAuthentication().getPrincipal();
        if(principal instanceof String){
            this.loginAttemptService.evictUserFromLoginAttemptCache(
                    (String)  principal
            );
        }
    }

}
