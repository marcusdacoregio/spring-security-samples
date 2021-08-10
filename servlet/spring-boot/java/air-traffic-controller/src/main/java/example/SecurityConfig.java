/*
 * Copyright 2021 the original author or authors.
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

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
		// @formatter:off
		http.authorizeRequests((authz) -> authz
				.antMatchers(HttpMethod.PUT, "/requests/*").hasRole("PILOT")
				.antMatchers(HttpMethod.POST, "/requests/*/approval").hasRole("CAPTAIN")
				.anyRequest().authenticated());
		http.httpBasic();
		// @formatter:on
		return http.build();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails josh = User.withUsername("josh")
				.password("{bcrypt}$2a$10$K0tArQLT0iWglT5PMrxyt.2iYYiwnrW05p93cXlsyXJuwZqyJXIm.")
				.roles("CAPTAIN")
				.build();
		UserDetails marcus = User.withUsername("marcus")
				.password("{noop}password")
				.roles("PILOT")
				.build();
		UserDetails steve = User.withUsername("steve")
				.password("{noop}password")
				.roles("PILOT")
				.build();
		return new InMemoryUserDetailsManager(josh, marcus, steve);
	}

}
