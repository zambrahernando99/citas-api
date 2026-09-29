package co.academy.citas.application.port.in;

public record TokenPair(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresInSeconds,
        long refreshTokenExpiresInSeconds) {
}
