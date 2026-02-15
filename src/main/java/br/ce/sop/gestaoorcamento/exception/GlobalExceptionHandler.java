package br.ce.sop.gestaoorcamento.exception;

import br.ce.sop.gestaoorcamento.exception.RecursoNaoEncontradoException;
import br.ce.sop.gestaoorcamento.exception.RegraDeNegocioException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<?> handleRegra(RegraDeNegocioException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "erro", "Regra de Negócio",
                        "mensagem", ex.getMessage()
                ));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<?> handleNotFound(RecursoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "status", HttpStatus.NOT_FOUND.value(),
                        "erro", "Recurso não encontrado",
                        "mensagem", ex.getMessage()
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException ex) {
        String rootCause = ex.getRootCause() != null
                ? ex.getRootCause().getMessage()
                : "";

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        ex.printStackTrace(); // APENAS PRA DEBUG EM DESENVOLVIMENTO
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "status", 500,
                        "erro", "Erro interno",
                        "mensagem", "Ocorreu um erro inesperado."
                ));
    }
}
