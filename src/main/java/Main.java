import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"controller", "service", "model"})
public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Maze Solver API...");
        SpringApplication.run(Main.class, args);
        System.out.println("Maze API Active on Port 8080.");
    }
}
