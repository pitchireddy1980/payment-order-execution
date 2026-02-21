package com.example.paymentsystem.exception;

import com.example.paymentsystem.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("ResourceNotFoundException returns 404 and error response")
    void handleResourceNotFoundException_Returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Payment order not found with ID: 999");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleResourceNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Payment order not found with ID: 999", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    @DisplayName("InvalidOperationException returns 400 and error response")
    void handleInvalidOperationException_Returns400() {
        InvalidOperationException ex = new InvalidOperationException("Cannot update order in status: COMPLETED");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleInvalidOperationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Cannot update order in status: COMPLETED", response.getBody().getMessage());
    }

    @Test
    @DisplayName("MethodArgumentNotValidException returns 400 with field errors")
    void handleValidationExceptions_Returns400WithFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("paymentOrderDTO", "customerId", "Customer ID is required");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        @SuppressWarnings("unchecked")
        ResponseEntity<ApiResponse<Map<String, String>>> response =
                (ResponseEntity<ApiResponse<Map<String, String>>>) (ResponseEntity<?>)
                        exceptionHandler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Validation failed", response.getBody().getMessage());
        Map<String, String> errors = response.getBody().getData();
        assertNotNull(errors);
        assertTrue(errors.containsKey("customerId"));
        assertEquals("Customer ID is required", errors.get("customerId"));
    }

    @Test
    @DisplayName("Generic Exception returns 500 and error response")
    void handleGlobalException_Returns500() {
        Exception ex = new RuntimeException("Unexpected database error");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleGlobalException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Unexpected database error"));
    }
}
