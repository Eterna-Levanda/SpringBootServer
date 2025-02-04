package and.learn.exceptionhandler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionsHandler {

    /*Questo catturatore non verrà mai richiamato se l'eccezione è lanciata dal Controller perchè in quella classe ce n'è già uno definito */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body("Errore catturato all'interno della classe ExceptionsHandler, metodo handleIllegalArgument: " + ex.getMessage());  // HTTP 400
    }


    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<String> handleNullPointerException(NullPointerException ex) {
        return ResponseEntity.internalServerError().body("Errore catturato all'interno della classe ExceptionsHandler, metodo NullPointerException: " + ex.getMessage());  // HTTP 500
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        return ResponseEntity.internalServerError().body("Errore generico catturato all'interno della classe ExceptionsHandler, metodo handleException: " + ex.getMessage());  // HTTP 500
    }
}
