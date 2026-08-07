package com.gabrielformario.credit_recovery_api.exception;

import com.gabrielformario.credit_recovery_api.strategy.exception.StrategyNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException exception) {
		Map<String, String> errors = new LinkedHashMap<>();

		exception.getBindingResult().getFieldErrors()
				.forEach(fieldError -> errors.put(fieldError.getField(), fieldError.getDefaultMessage()));

		return ResponseEntity.badRequest().body(new ApiErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				"Validation failed",
				errors
		));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidRequestBody() {
		return ResponseEntity.badRequest().body(new ApiErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				"Invalid request body",
				Map.of()
		));
	}

	@ExceptionHandler(StrategyNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleStrategyNotFound(StrategyNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(
				HttpStatus.NOT_FOUND.value(),
				exception.getMessage(),
				Map.of()
		));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpectedErrors() {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse(
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Unexpected error",
				Map.of()
		));
	}

	private record ApiErrorResponse(
			int status,
			String message,
			Map<String, String> errors
	) {
	}
}
