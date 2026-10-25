package com.acme.center.platform.shared.interfaces.rest.transform;

import com.acme.center.platform.shared.application.result.ApplicationError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.net.URI;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProblemDetailFromApplicationErrorAssemblerTest {

    @AfterEach
    void clearLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void toResponseFromApplicationErrorUsesDefaultBundleMessage() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        var error = ApplicationError.notFound("Profile", "123");
        var response = ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(error);
        var problemDetail = response.getBody();

        assertNotNull(problemDetail);
        assertEquals(404, response.getStatusCode().value());
        assertEquals(404, problemDetail.getStatus());
        assertEquals("PROFILE_NOT_FOUND", problemDetail.getProperties().get("code"));
        assertEquals("Profile not found", problemDetail.getDetail());
        assertEquals(URI.create("/api/v1/errors/not-found"), problemDetail.getType());
        assertEquals("Resource Not Found", problemDetail.getTitle());
    }

    @Test
    void toResponseFromApplicationErrorUsesSpanishBundleMessage() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));

        var error = ApplicationError.conflict("User", "Username already exists");
        var response = ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(error);
        var problemDetail = response.getBody();

        assertNotNull(problemDetail);
        assertEquals(409, response.getStatusCode().value());
        assertEquals(409, problemDetail.getStatus());
        assertEquals("USER_CONFLICT", problemDetail.getProperties().get("code"));
        assertEquals("El usuario ya existe", problemDetail.getDetail());
        assertEquals(URI.create("/api/v1/errors/conflict"), problemDetail.getType());
        assertEquals("Resource Conflict", problemDetail.getTitle());
    }

    @Test
    void toResponseFromApplicationErrorFallsBackToCategoryKeyWhenSpecificIsMissing() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));

        var error = ApplicationError.notFound("Course", "42");
        var response = ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(error);
        var problemDetail = response.getBody();

        assertNotNull(problemDetail);
        assertEquals(404, response.getStatusCode().value());
        assertEquals(404, problemDetail.getStatus());
        assertEquals("COURSE_NOT_FOUND", problemDetail.getProperties().get("code"));
        assertEquals("Recurso no encontrado", problemDetail.getDetail());
        assertEquals(URI.create("/api/v1/errors/not-found"), problemDetail.getType());
    }
}
