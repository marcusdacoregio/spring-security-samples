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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RequestsController.class)
@Import(SecurityConfig.class)
class RequestsControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RequestsService requestsService;

	@Test
	@WithMockUser(roles = "PILOT")
	void createRequestWhenPilotThenOk() throws Exception {
		this.mockMvc.perform(put("/requests/3232").with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	void createRequestWhenUnauthenticatedThenForbidden() throws Exception {
		this.mockMvc.perform(put("/requests/3232"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "CAPTAIN")
	void approveRequestWhenCaptainThenOk() throws Exception {
		this.mockMvc.perform(
				post("/requests/3232/approval")
						.content("{ \"approved\": true }")
						.contentType(MediaType.APPLICATION_JSON)
						.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "PILOT")
	void approveRequestWhenPilotThenForbidden() throws Exception {
		this.mockMvc.perform(
				post("/requests/3232/approval")
						.content("{ \"approved\": true }")
						.contentType(MediaType.APPLICATION_JSON)
						.with(csrf()))
				.andExpect(status().isForbidden());
	}
}