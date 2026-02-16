package br.ce.sop.gestaoorcamento.exception;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Erros de Segurança (Login Inválido)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials() {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Não Autorizado", "Usuário ou senha inválidos.");
    }

    // 2. Erros de Segurança (Permissão Insuficiente - @PreAuthorize)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied() {
        return buildResponse(HttpStatus.FORBIDDEN, "Acesso Negado", "Você não tem permissão para executar esta ação.");
    }

    // 3. Erros de Token (JWT Expirado ou Inválido)
    @ExceptionHandler({TokenExpiredException.class, JWTVerificationException.class})
    public ResponseEntity<?> handleJwtErrors(Exception ex) {
        String msg = ex instanceof TokenExpiredException ? "Token expirado. Faça login novamente." : "Token inválido.";
        return buildResponse(HttpStatus.UNAUTHORIZED, "Token Inválido", msg);
    }

    // 4. Erros de Validação (@Valid nos DTOs)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        var erros = ex.getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildResponse(HttpStatus.BAD_REQUEST, "Erro de Validação", erros);
    }

    // 5. Regras de Negócio e Recurso não encontrado
    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<?> handleRegra(RegraDeNegocioException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Regra de Negócio", ex.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<?> handleNotFound(RecursoNaoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage());
    }

    // 6. Integridade de Dados (Seu código melhorado)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException ex) {
        String rootCause = ex.getRootCause() != null ? ex.getRootCause().getMessage() : "";
        String mensagem = "Violação de restrição de banco de dados.";

        if (rootCause.contains("orcamento_numero_protocolo_key")) mensagem = "O número de protocolo informado já está em uso.";
        else if (rootCause.contains("medicao_numero_medicao_key")) mensagem = "O número de medição informado já existe.";
        else if (rootCause.contains("item_medicao_medicao_id_item_id_key")) mensagem = "Este item já foi incluído na medição atual.";

        return buildResponse(HttpStatus.CONFLICT, "Conflito de Dados", mensagem);
    }

    // 7. Erro Genérico (Fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        ex.printStackTrace();
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro Interno", "Ocorreu um erro inesperado no servidor.");
    }

    // Captura erros de autenticação interna (como o contrato do UserDetailsService violado)
    @ExceptionHandler(org.springframework.security.authentication.InternalAuthenticationServiceException.class)
    public ResponseEntity<?> handleInternalAuth(Exception ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Não Autorizado", "Usuário ou senha inválidos.");
    }

    // Opcional: capturar a exceção base de segurança para garantir que nada passe
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(org.springframework.security.core.AuthenticationException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Não Autorizado", "Falha na autenticação.");
    }

    // Método auxiliar para padronizar o JSON de resposta
    private ResponseEntity<?> buildResponse(HttpStatus status, String erro, String mensagem) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "erro", erro,
                "mensagem", mensagem
        ));
    }
}