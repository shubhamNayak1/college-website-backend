package com.baseras.portal.common;

import com.baseras.portal.entity.User;
import com.baseras.portal.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrentUser {

    private final UserRepository users;

    public CurrentUser(UserRepository users) {
        this.users = users;
    }

    public Optional<String> userIdOpt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return Optional.empty();
        Object principal = auth.getPrincipal();
        return principal instanceof String s && !"anonymousUser".equals(s) ? Optional.of(s) : Optional.empty();
    }

    public String requireUserId() {
        return userIdOpt().orElseThrow(() -> new AppExceptions.UnauthorizedException("Authentication required"));
    }

    public User require() {
        String id = requireUserId();
        return users.findById(id).orElseThrow(() -> new AppExceptions.UnauthorizedException("User not found"));
    }

    public Optional<User> opt() {
        return userIdOpt().flatMap(users::findById);
    }
}
