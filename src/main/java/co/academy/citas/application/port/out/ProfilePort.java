package co.academy.citas.application.port.out;

import java.util.UUID;

public interface ProfilePort {
    void update(UUID userId, String givenNames, String familyNames, String email, String phone);
}
