package com.vsbp.web;

import com.vsbp.domain.AppUser;
import com.vsbp.domain.Role;
import java.io.Serializable;

/** What is kept in the HTTP session after login (never the password hash). */
public class SessionUser implements Serializable {

    public static final String KEY = "sessionUser";

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String name;
    private final Role role;

    public SessionUser(Long id, String name, Role role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public static SessionUser from(AppUser user) {
        return new SessionUser(user.getId(), user.getFullName(), user.getRole());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
