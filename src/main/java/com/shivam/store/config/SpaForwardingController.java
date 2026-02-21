package com.shivam.store.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class SpaForwardingController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping(value = {"/orders", "/orders/success", "/products/{id:[0-9]+}"}, produces = MediaType.TEXT_HTML_VALUE)
    public String forwardHtmlConflictRoutes() {
        return "forward:/index.html";
    }

    @RequestMapping(value = {
            "/{path:^(?!api|auth|users|products|carts|checkout|orders|swagger-ui|v3|h2-console|admin|assets)[^\\.]*}",
            "/{path:^(?!api|auth|users|products|carts|checkout|orders|swagger-ui|v3|h2-console|admin|assets)[^\\.]*}/{*remaining}"
    })
    public String forward(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (hasFileExtension(uri)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return "forward:/index.html";
    }

    private boolean hasFileExtension(String uri) {
        int lastSlash = uri.lastIndexOf('/');
        int lastDot = uri.lastIndexOf('.');
        return lastDot > lastSlash;
    }
}
