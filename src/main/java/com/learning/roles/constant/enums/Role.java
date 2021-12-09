package com.learning.roles.constant.enums;

import com.learning.roles.constant.Authority;

import java.util.Set;

public enum Role {

    ROLE_USER(Authority.USER_AUTHORITIES),
    ROLE_MANAGER(Authority.MANAGER_AUTHORITIES),
    ROLE_ADMIN(Authority.ADMIN_AUTHORITIES),
    ROLE_SUPER(Authority.SUPER_AUTHORITIES);

    private final String[] permissions;

    Role(String[] permissions) {
        this.permissions = permissions;
    }

    public String[] getPermissions() {
        return permissions;
    }
}
