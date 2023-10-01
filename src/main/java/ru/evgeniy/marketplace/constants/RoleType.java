package ru.evgeniy.marketplace.constants;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RoleType {

    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    private final String roleWithPrefix;

    private final static String PREFIX = "ROLE_";

    public static String getPreparedRole(String role) {
        if (role == null) {
            return RoleType.USER.roleWithPrefix;
        }
        return role.startsWith(PREFIX) ? role : PREFIX + role;
    }

    public String getRole() {
        return roleWithPrefix;
    }
}