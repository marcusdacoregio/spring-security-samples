/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello Security application.
 *
 * @author Joe Grandja
 * @author Marcus Hert da Coregio
 */
@SpringBootApplication
@RestController
public class HelloSecurityApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(HelloSecurityApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(HelloSecurityApplication.class);
	}

	@Bean
	public SecurityFilterChain formLoginFilterChain(HttpSecurity http) throws Exception {
		// @formatter:off
		http
				.authorizeRequests(authorize -> authorize
						.anyRequest().authenticated()
				)
				.formLogin((form) -> form
						.loginPage("/api/login").permitAll()
				);
		// @formatter:on
		return http.build();
	}


	@GetMapping("/hello")
	public String hello(@AuthenticationPrincipal User user) {
		return "Hello " + user.getUsername() + "!";
	}
}
