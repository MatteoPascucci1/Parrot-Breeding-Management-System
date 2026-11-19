package com.example.demo.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Richiesta incompleta: Scatta quando falliscono le annotazioni @NotBlank o @NotNull collegate a @Valid			GESTIONE @VALID (Campi mancanti o vuoti) -> 400 Bad Request
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // 2. JSON non valido: Scatta se il client invia sintassi errata (es. commenti //), virgolette mancanti o tipi sbagliati		GESTIONE JSON MALFORMATO (Es. commenti //, errori di sintassi) -> 400 Bad Request
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleJsonParseException(HttpMessageNotReadableException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid JSON");
        error.put("message", "The request body contains incorrect syntax or incompatible data types.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 3. Conflitti di dati nel Database: Scatta su valori UNIQUE duplicati (es. ringNumber o cageName già esistenti)		GESTIONE CONFLITTI DATABASE (Es. valori UNIQUE già esistenti) -> 409 Conflict
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Data integrity conflict");
        error.put("message", "The operation could not be completed: a unique value you entered (such as a cage name or ring number) already exists in the database or violates a constraint.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 4. Tipo di parametro nell'URL sbagliato: Scatta se invii /api/parrots/abc invece di un numero ID		GESTIONE PARAMETRI URL ERRATI (Es. /api/parrots/abc invece di un numero) -> 400 Bad Request
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Incorrect parameter in the URL");
        error.put("message", "The value'" + ex.getValue() + "' provided in the URL is not valid for the parameter " + ex.getName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 5. Permessi insufficienti: Scatta quando @PreAuthorize blocca un ruolo non autorizzato -> 403 Forbidden
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Forbidden");
        error.put("message", "You do not have the required permissions to perform this action.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    // 5. Paracadute di sicurezza generico: Intercetta qualsiasi altro errore imprevisto trasformandolo in un 400 controllato		GESTIONE ERRORI GENERICI (Evita il 500 in ogni altro caso) -> 400 Bad Request
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid request");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}