package Fint.FinTribe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FinTribeApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinTribeApplication.class, args);
	}

}