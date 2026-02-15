package br.ce.sop.gestaoorcamento.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException ex) {
        // Pegamos a causa raiz para ser mais preciso
        String rootCause = ex.getRootCause() != null ? ex.getRootCause().getMessage() : "";

        String mensagem = "Erro de integridade: Violação de restrição de banco de dados.";

        if (rootCause.contains("orcamento_numero_protocolo_key")) {
            mensagem = "Conflito de cadastro: O número de protocolo informado já está em uso.";
        } else if (rootCause.contains("medicao_numero_medicao_key")) {
            mensagem = "Conflito de cadastro: O número de medição informado já existe para outro registro.";
        } else if (rootCause.contains("item_medicao_medicao_id_item_id_key")) {
            mensagem = "Item duplicado: Este item já foi incluído na medição atual.";
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "status", HttpStatus.CONFLICT.value(),
                        "erro", "Conflito de Dados",
                        "mensagem", mensagem
                ));
    }
}
