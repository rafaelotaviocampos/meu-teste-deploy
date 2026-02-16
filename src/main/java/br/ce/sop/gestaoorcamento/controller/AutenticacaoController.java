package br.ce.sop.gestaoorcamento.controller;

import br.ce.sop.gestaoorcamento.config.security.TokenService;
import br.ce.sop.gestaoorcamento.dto.AutenticacaoDTO;
import br.ce.sop.gestaoorcamento.dto.TokenResponseDTO;
import br.ce.sop.gestaoorcamento.model.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/login")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoint para login e geração de token JWT")
public class AutenticacaoController {

    private final AuthenticationManager manager;
    private final TokenService tokenService;

    @PostMapping
    @Operation(summary = "Efetuar login", description = "Recebe login/senha e retorna um token JWT válido")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid AutenticacaoDTO dto) {
        // 1. Cria o token interno do Spring com as credenciais recebidas
        var authenticationToken = new UsernamePasswordAuthenticationToken(dto.login(), dto.senha());

        // 2. O Manager chama o AutenticacaoService e valida a senha (BCrypt)
        var authentication = manager.authenticate(authenticationToken);

        // 3. Se passou, gera o JWT para o usuário autenticado
        var tokenJWT = tokenService.gerarToken((Usuario) authentication.getPrincipal());

        return ResponseEntity.ok(new TokenResponseDTO(tokenJWT));
    }
}