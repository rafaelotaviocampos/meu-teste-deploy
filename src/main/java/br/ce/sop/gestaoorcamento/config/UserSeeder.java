package br.ce.sop.gestaoorcamento.config;

import br.ce.sop.gestaoorcamento.model.Usuario;
import br.ce.sop.gestaoorcamento.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserSeeder implements CommandLineRunner {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            log.info("Criando usuário padrão admin...");
            Usuario admin = new Usuario();
            admin.setLogin("admin");
            admin.setRole("ROLE_ADMIN");
            // O próprio bean que o Spring usa para validar vai gerar o hash
            admin.setSenha(passwordEncoder.encode("123456"));

            repository.save(admin);
            log.info("Usuário admin criado com sucesso!");
        }
    }
}