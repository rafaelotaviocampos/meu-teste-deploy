package br.ce.sop.gestaoorcamento.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestão de Orçamentos e Medições - SOP")
                        .version("1.0")
                        .description("Sistema para controle de orçamentos de obras e validação de medições.")
                        .contact(new Contact()
                                .name("Rafael Campos")
                                .email("only_roc@hotmail.com")));
    }
}
