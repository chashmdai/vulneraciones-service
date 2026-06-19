package cl.smid.vulneraciones.api.error;

import cl.smid.vulneraciones.dominio.excepcion.ErrorDominioException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * Manejador global de errores: traduce las excepciones a un {@link ErrorResponse} unificado.
 *
 * <p>Las excepciones de dominio fijan su propio código y estado HTTP; los errores de validación y de
 * cuerpo ilegible se mapean a VUL-001 (400) con detalles; cualquier otra excepción no controlada se
 * traduce a VUL-500 (500) sin filtrar internals. La ruta se toma de {@code getRequestURI()} y se
 * expone en el campo {@code ruta}.</p>
 */
@RestControllerAdvice
public class ManejadorGlobalErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalErrores.class);

    @ExceptionHandler(ErrorDominioException.class)
    public ResponseEntity<ErrorResponse> manejarDominio(ErrorDominioException ex, HttpServletRequest request) {
        HttpStatus estado = HttpStatus.valueOf(ex.getCodigo().httpStatus());
        ErrorResponse cuerpo = ErrorResponse.de(
                estado.value(),
                estado.getReasonPhrase(),
                ex.getCodigo().codigo(),
                ex.getMessage(),
                ex.getDetalles(),
                request.getRequestURI());
        return ResponseEntity.status(estado).body(cuerpo);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException ex,
                                                           HttpServletRequest request) {
        List<String> detalles = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            detalles.add(error.getField() + ": " + error.getDefaultMessage());
        }
        return respuesta(HttpStatus.BAD_REQUEST, "VUL-001",
                "La solicitud contiene errores de validación.", detalles, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> manejarRestricciones(ConstraintViolationException ex,
                                                             HttpServletRequest request) {
        List<String> detalles = new ArrayList<>();
        for (ConstraintViolation<?> violacion : ex.getConstraintViolations()) {
            detalles.add(violacion.getPropertyPath() + ": " + violacion.getMessage());
        }
        return respuesta(HttpStatus.BAD_REQUEST, "VUL-001",
                "La solicitud contiene parámetros inválidos.", detalles, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarCuerpoIlegible(HttpMessageNotReadableException ex,
                                                             HttpServletRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST, "VUL-001",
                "El cuerpo de la solicitud es ilegible o está mal formado.", List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarGenerico(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado procesando {} {}", request.getMethod(), request.getRequestURI(), ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "VUL-500",
                "Ocurrió un error interno al procesar la solicitud.", List.of(), request);
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus estado, String codigo, String mensaje,
                                                   List<String> detalles, HttpServletRequest request) {
        ErrorResponse cuerpo = ErrorResponse.de(
                estado.value(), estado.getReasonPhrase(), codigo, mensaje, detalles, request.getRequestURI());
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
