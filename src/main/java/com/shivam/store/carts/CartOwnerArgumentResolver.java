package com.shivam.store.carts;

import com.shivam.store.config.JwtConfig;
import com.shivam.store.services.AuthService;
import com.shivam.store.services.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class CartOwnerArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String GUEST_TOKEN_COOKIE = "guestToken";

    private final AuthService authService;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    @Value("${app.cookies.secure:false}")
    private boolean secureCookies;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return CartOwner.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        if (request == null) {
            throw new IllegalStateException("Missing HttpServletRequest");
        }

        var authenticatedUser = authService.findAuthenticatedUser();
        if (authenticatedUser.isPresent()) {
            // Clear guest token cookie if present
            clearGuestCookie(response);
            return CartOwner.authenticated(authenticatedUser.get());
        }

        String guestToken = extractGuestToken(request)
                .orElseGet(() -> issueGuestCookie(response));

        return CartOwner.guest(guestToken);
    }

    private Optional<String> extractGuestToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> GUEST_TOKEN_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue)
                // OWASP A04/A08: only trust signed, non-expired guest JWTs from cookies.
                .filter(this::isValidGuestToken)
                .findFirst();
    }

    private String issueGuestCookie(HttpServletResponse response) {
        if (response == null) {
            throw new IllegalStateException("Missing HttpServletResponse for guest token issuance");
        }
        String guestToken = jwtService.generateGuestToken().toString();
        var cookie = ResponseCookie.from(GUEST_TOKEN_COOKIE, guestToken)
                .httpOnly(true)
                .secure(secureCookies)
                .path("/")
                .maxAge(jwtConfig.getGuestExpiration())
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return guestToken;
    }

    private void clearGuestCookie(HttpServletResponse response) {
        if (response == null) {
            return;
        }
        var cookie = ResponseCookie.from(GUEST_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(secureCookies)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private boolean isValidGuestToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        var jwt = jwtService.parseToken(token);
        return jwt != null && !jwt.isExpired() && jwt.isGuest();
    }
}
