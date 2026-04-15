package com.payment.account.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(RuntimeException.class)
	 public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
		String message = ex.getMessage();
		if (message.contains("not found")) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", message));
        }
		 
		if (message.contains("blocked")) {
	            return ResponseEntity
	                    .status(HttpStatus.FORBIDDEN)
	                    .body(Map.of("error", message));
	        }
		
		 if (message.contains("Insufficient") || message.contains("greater than zero")) {
	            return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(Map.of("error", message));
	        }
		
	      return ResponseEntity
	                .status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", message));
	    }
	}


