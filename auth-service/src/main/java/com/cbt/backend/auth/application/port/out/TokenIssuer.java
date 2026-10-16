package com.cbt.backend.auth.application.port.out;

import com.cbt.backend.auth.domain.model.User;

public interface TokenIssuer {

    IssuedToken issue(User user);
}
