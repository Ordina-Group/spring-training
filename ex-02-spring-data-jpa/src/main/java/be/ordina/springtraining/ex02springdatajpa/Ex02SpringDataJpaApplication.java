package be.ordina.springtraining.ex02springdatajpa;

import be.ordina.springtraining.ex02springdatajpa.cache.UsersCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Ex02SpringDataJpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(Ex02SpringDataJpaApplication.class, args);
	}

	@Bean
	public UsersCache usersCache() {
		return new UsersCache();
	}

}
