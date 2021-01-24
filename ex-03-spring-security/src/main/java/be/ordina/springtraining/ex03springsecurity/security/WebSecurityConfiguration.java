package be.ordina.springtraining.ex03springsecurity.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers().frameOptions()
                .sameOrigin()
                .and()
            .csrf()
                .ignoringAntMatchers("/users/**", "/h2-console/**").and()
                .authorizeRequests()
                        .mvcMatchers(HttpMethod.GET, "/users").permitAll()
                        .antMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                        .and()
                .formLogin().and()
                .logout().and()
                .httpBasic();
    }

}