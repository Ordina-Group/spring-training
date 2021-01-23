package be.ordina.springtraining.ex01springmvc;

import be.ordina.springtraining.ex01springmvc.cache.UsersCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Ex01SpringMvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(Ex01SpringMvcApplication.class, args);
	}

	@Bean
	public UsersCache usersCache() {
		return new UsersCache();
	}

}
