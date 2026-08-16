package com.gamehub.security;

import com.gamehub.entity.User;
import com.gamehub.exception.UnauthorizedException;

public final class CurrentUser {

    private static final ThreadLocal<User> HOLDER = new ThreadLocal<>();

    private CurrentUser() {
    }

    public static void set(User user) {
        HOLDER.set(user);
    }

    public static User get() {
        return HOLDER.get();
    }

    /** Returns the current authenticated user or throws 401 if none. */
    public static User require() {
        User user = HOLDER.get();
        if (user == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return user;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
