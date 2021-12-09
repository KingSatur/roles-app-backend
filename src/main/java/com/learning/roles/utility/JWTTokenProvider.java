package com.learning.roles.utility;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.learning.roles.constant.SecurityConstant;
import com.learning.roles.domain.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;


@Component
public class JWTTokenProvider {

    @Value("${jwt.seed}")
    private String seed;


    //This method generates the token
    public String generateJwtToken(UserPrincipal userPrincipal){
        String[] claims = userPrincipal.getAuthorities().stream().
                map(m -> m.getAuthority()).collect(Collectors.toList()).toArray(new String[]{});
        return JWT.create().withIssuedAt(new Date()).withSubject(
                userPrincipal.getUsername())
                .withArrayClaim(SecurityConstant.AUTHORITIES, claims)
                .withExpiresAt(new Date(System.currentTimeMillis() +
                        SecurityConstant.EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(seed.getBytes()));
    }

    public List<GrantedAuthority> getAuthorities(String token){
        JWTVerifier jwtVerifier = this.getJWTVerifier();
        String[] claims = jwtVerifier.verify(token).getClaim(SecurityConstant.AUTHORITIES).asArray(String.class);
        return Arrays.stream(claims).map(m -> new SimpleGrantedAuthority(m)).collect(Collectors.toList());

    }

    public Authentication getAuthentication(String username, List<GrantedAuthority> authorities, HttpServletRequest request){
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
        usernamePasswordAuthenticationToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );
        return usernamePasswordAuthenticationToken;
    }

    public boolean isTokenValid(String username, String token){
        JWTVerifier jwtVerifier = this.getJWTVerifier();
        return StringUtils.hasLength(token)
                && StringUtils.hasText(token)
                && !isTokenExpired(jwtVerifier, token);
    }

    private boolean isTokenExpired(JWTVerifier verifier, String token){
        Date expiration = verifier.verify(token).getExpiresAt();
        return expiration.before(new Date());
    }

    public String getSubject(String token){
        JWTVerifier verifier = getJWTVerifier();
        return verifier.verify(token).getSubject();
    }

    public JWTVerifier getJWTVerifier(){
        JWTVerifier jwtVerifier;
        try{
            Algorithm algorithm = Algorithm.HMAC512(seed.getBytes());
            jwtVerifier = JWT.require(algorithm).build();
            return jwtVerifier;
        }
        catch (Exception e){
            throw  new JWTVerificationException(SecurityConstant.TOKEN_CANNOT_BE_VERIFIED);
        }
    }


}
