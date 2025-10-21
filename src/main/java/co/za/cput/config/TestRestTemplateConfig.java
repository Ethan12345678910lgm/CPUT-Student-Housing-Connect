package co.za.cput.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.ObjectProvider;

@Configuration
@Profile("test")
public class TestRestTemplateConfig {

    @Bean
    ApplicationRunner configureTestRestTemplateErrorHandler(ObjectProvider<TestRestTemplate> testRestTemplateProvider) {
        return args -> testRestTemplateProvider.ifAvailable(testRestTemplate ->
                testRestTemplate.getRestTemplate()
                        .setErrorHandler(new SelectiveNotFoundErrorHandler())
        );
    }
}