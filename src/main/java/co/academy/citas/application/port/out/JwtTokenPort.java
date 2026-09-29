package co.academy.citas.application.port.out;

import co.academy.citas.domain.account.UserAccount;
import java.util.UUID;

public interface JwtTokenPort {
    IssuedToken issueAccess(UserAccount userAccount);
    IssuedToken issueRefresh(UserAccount userAccount, UUID sessionId);
    AccessTokenClaims parseAccess(String token);
    RefreshTokenClaims parseRefresh(String token);
}
