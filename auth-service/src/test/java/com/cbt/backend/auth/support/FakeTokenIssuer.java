package com.cbt.backend.auth.support;

import com.cbt.backend.auth.application.port.out.IssuedToken;
import com.cbt.backend.auth.application.port.out.TokenIssuer;
import com.cbt.backend.auth.domain.model.User;

public class FakeTokenIssuer implements TokenIssuer {

    public static final long TTL_SECONDS = 3600;

    private int issuedCount;

    @Override
    public IssuedToken issue(User user) {
        issuedCount++;
        return new IssuedToken("token-for-" + user.id(), TTL_SECONDS);
    }

    public int issuedCount() {
        return issuedCount;
    }
}
