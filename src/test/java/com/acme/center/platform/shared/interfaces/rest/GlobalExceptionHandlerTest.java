package com.acme.center.platform.shared.interfaces.rest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @AfterEach
    void clearLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void handleRuntimeExceptionUsesLocalizedUnexpectedMessage() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));

        var handler = new GlobalExceptionHandler();
        var response = handler.handleRuntimeException(new RuntimeException("boom"));
        var problemDetail = Objects.requireNonNull(response.getBody());

        assertEquals(500, response.getStatusCode().value());
        assertEquals(500, problemDetail.getStatus());
        assertEquals("UNEXPECTED_ERROR", problemDetail.getProperties().get("code"));
        assertEquals("Error inesperado", problemDetail.getDetail());
        assertEquals("boom", problemDetail.getProperties().get("details"));
        assertEquals(URI.create("/api/v1/errors/unexpected-error"), problemDetail.getType());
    }

    @Test
    void handleIllegalArgumentExceptionReturnsValidationError() {
        var handler = new GlobalExceptionHandler();
        var response = handler.handleIllegalArgumentException(
                new IllegalArgumentException("Student record id must be a valid UUID")
        );
        var problemDetail = Objects.requireNonNull(response.getBody());

        assertEquals(400, response.getStatusCode().value());
        assertEquals(400, problemDetail.getStatus());
        assertEquals("VALIDATION_ERROR", problemDetail.getProperties().get("code"));
        assertEquals("Validation failed", problemDetail.getDetail());
        assertEquals("Student record id must be a valid UUID", problemDetail.getProperties().get("details"));
        assertEquals(URI.create("/api/v1/errors/validation-error"), problemDetail.getType());
    }
}


