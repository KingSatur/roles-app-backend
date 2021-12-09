package com.learning.roles.listener;

import com.learning.roles.service.LoginAttemptService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthFailedListener {

    private final LoginAttemptService loginAttemptService;

    public AuthFailedListener(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }


    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event){
        Object principal = event.getAuthentication().getPrincipal();
        if(principal instanceof String){
            this.loginAttemptService.addUserToLoginAttemptCache(
                    (String)  principal
            );
        }
    }
}
