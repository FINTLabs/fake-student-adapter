package no.fint;

import com.github.springfox.loader.EnableSpringfox;
import no.fint.oauth.OAuthConfig;
import no.fint.oauth.OAuthTokenProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@EnableSpringfox
@EnableScheduling
@Import(OAuthConfig.class)
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @ConditionalOnProperty(name = OAuthTokenProps.ENABLE_OAUTH,
            matchIfMissing = true, havingValue = "false")
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
