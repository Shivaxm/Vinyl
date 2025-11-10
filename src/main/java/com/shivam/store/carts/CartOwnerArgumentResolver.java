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
import org.springframework.core.MethodParameter;
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

        var guestTokenOpt = extractGuestToken(request);
        var authenticatedUser = authService.findAuthenticatedUser();
        if (authenticatedUser.isPresent()) {
            
            return CartOwner.authenticated(authenticatedUser.get(), guestTokenOpt);
        }

        String guestToken = guestTokenOpt
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
                .filter(token -> token != null && !token.isBlank())
                .findFirst();
    }

    private String issueGuestCookie(HttpServletResponse response) {
        if (response == null) {
            throw new IllegalStateException("Missing HttpServletResponse for guest token issuance");
        }
        String guestToken = jwtService.generateGuestToken().toString();
        Cookie cookie = new Cookie(GUEST_TOKEN_COOKIE, guestToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(jwtConfig.getGuestExpiration());
        cookie.setSecure(true);
        response.addCookie(cookie);
        return guestToken;
    }
}
