package com.acme.center.platform.shared.interfaces.rest;

import com.acme.center.platform.learning.domain.model.valueobjects.AcmeStudentRecordId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LocalizedErrorResponseWebMvcTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");

        var validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        validator.afterPropertiesSet();

        var localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        localeResolver.setSupportedLocales(List.of(Locale.ENGLISH, Locale.forLanguageTag("es")));

        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setLocaleResolver(localeResolver)
                .build();
    }

    @Test
    void returnsSpanishLocalizedMessageFromAcceptLanguageHeader() throws Exception {
        mockMvc.perform(get("/test/error")
                        .header("Accept-Language", "es")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("UNEXPECTED_ERROR"))
                .andExpect(jsonPath("$.detail").value("Error inesperado"))
                .andExpect(jsonPath("$.details").value("boom"))
                .andExpect(jsonPath("$.type").value("/api/v1/errors/unexpected-error"));
    }

    @Test
    void returnsEnglishLocalizedMessageFromAcceptLanguageHeader() throws Exception {
        mockMvc.perform(get("/test/error")
                        .header("Accept-Language", "en")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("UNEXPECTED_ERROR"))
                .andExpect(jsonPath("$.detail").value("Unexpected error"))
                .andExpect(jsonPath("$.details").value("boom"))
                .andExpect(jsonPath("$.type").value("/api/v1/errors/unexpected-error"));
    }

    @Test
    void returnsSpanishLocalizedValidationDetailsFromAcceptLanguageHeader() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .header("Accept-Language", "es")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.detail").value("Validacion fallida"))
                .andExpect(jsonPath("$.details").value("Campo name: es obligatorio"))
                .andExpect(jsonPath("$.type").value("/api/v1/errors/validation-error"))
                .andExpect(jsonPath("$.invalidParams[0].field").value("name"))
                .andExpect(jsonPath("$.invalidParams[0].message").value("es obligatorio"));
    }

    @Test
    void fallsBackToEnglishWhenLocaleIsUnsupported() throws Exception {
        mockMvc.perform(get("/test/error")
                        .header("Accept-Language", "fr")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("UNEXPECTED_ERROR"))
                .andExpect(jsonPath("$.detail").value("Unexpected error"))
                .andExpect(jsonPath("$.details").value("boom"))
                .andExpect(jsonPath("$.type").value("/api/v1/errors/unexpected-error"));
    }

    @Test
    void returnsBadRequestForInvalidUuidStudentRecordId() throws Exception {
        mockMvc.perform(get("/test/students/not-a-uuid")
                        .header("Accept-Language", "en")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.details").value("Student record id must be a valid UUID"))
                .andExpect(jsonPath("$.type").value("/api/v1/errors/validation-error"));
    }

    @RestController
    static class ThrowingTestController {
        @GetMapping("/test/error")
        public String error() {
            throw new RuntimeException("boom");
        }

        @PostMapping("/test/validation")
        public String validate(@Valid @RequestBody ValidationResource resource) {
            return resource.name();
        }

        @GetMapping("/test/students/{studentRecordId}")
        public String student(@org.springframework.web.bind.annotation.PathVariable String studentRecordId) {
            return new AcmeStudentRecordId(studentRecordId).studentRecordId();
        }
    }

    record ValidationResource(@NotBlank(message = "{validation.not-blank}") String name) {
    }
}


