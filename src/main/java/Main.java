import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@ComponentScan(basePackages = {"controller", "service", "model"})
public class Main {

    public static void main(String[] args) {
        System.out.println("Starting AlgoPath Visualizer API...");
        SpringApplication.run(Main.class, args);
    }

    /**
     * Global CORS policy — allows the Netlify frontend to call this API.
     * Set ALLOWED_ORIGIN env var to restrict to your specific Netlify URL in prod.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String origin = System.getenv("ALLOWED_ORIGIN");
                String allowedOrigin = (origin != null && !origin.isBlank()) ? origin : "*";
                registry.addMapping("/**")
                        .allowedOriginPatterns(allowedOrigin)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
