package com.learning.roles.constant;

public class SecurityConstant {

    public static final long EXPIRATION_TIME = 432000000;
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String JWT_TOKEN_HEADER = "Jwt-Token";
    public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verefied";
    public static final String PROVIDED_BY = "JD_DEVELOPER";
    public static final String AUTHORITIES = "authorities";
    public static final String FORBIDDEN_MESSAGE = "You need to log in to access this resource";
    public static final String ACCESS_DENIED_MESSAGE = "You cannot access this resource";
    public static final String OPTIONS_HTTP_METHOD = "OPTIONS";
    public static final String[] PUBLIC_URLS = { "/user/login", "/user/register", "/user/reset-password/**", "/user/image/**"};
}
