/*
 * Copyright 2002-2021 the original author or authors.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.htmlunit.LocalHostWebClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder;
import org.springframework.util.Assert;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class Saml2LoginApplicationITests {

	@Autowired
	MockMvc mvc;

	@Autowired
	WebClient webClient;

	@BeforeEach
	void setup() {
		this.webClient.getCookieManager().clearCookies();
	}

	@Test
	void authenticationAttemptWhenValidThenShowsUserEmailAddress() throws Exception {
		performLogin();
		HtmlPage home = (HtmlPage) this.webClient.getCurrentWindow().getEnclosedPage();
		assertThat(home.asNormalizedText()).contains("You're email address is testuser2@spring.security.saml");
	}

	@Test
	void logoutWhenRelyingPartyInitiatedLogoutThenLoginPageWithLogoutParam() throws Exception {
		performLogin();
		HtmlPage home = (HtmlPage) this.webClient.getCurrentWindow().getEnclosedPage();
		HtmlElement rpLogoutButton = home.getHtmlElementById("rp_logout_button");
		HtmlPage loginPage = rpLogoutButton.click();
		this.webClient.waitForBackgroundJavaScript(10000);
		List<String> urls = new ArrayList<>();
		urls.add(loginPage.getUrl().getFile());
		urls.add(((HtmlPage) this.webClient.getCurrentWindow().getEnclosedPage()).getUrl().getFile());
		assertThat(urls).withFailMessage(() -> {
			// @formatter:off
			String builder = loginPage.asXml()
					+ "\n\n\n"
					+ "Enclosing Page"
					+ "\n\n\n"
					+ ((HtmlPage) this.webClient.getCurrentWindow().getEnclosedPage()).asXml();
			// @formatter:on
			return builder;
		}).contains("/login?logout");
	}

	private void performLogin() throws Exception {
		HtmlPage login = this.webClient.getPage("/");
		this.webClient.waitForBackgroundJavaScript(10000);
		HtmlForm form = findForm(login);
		HtmlInput username = form.getInputByName("username");
		HtmlPasswordInput password = form.getInputByName("password");
		HtmlSubmitInput submit = login.getHtmlElementById("okta-signin-submit");
		username.type("testuser2@spring.security.saml");
		password.type("12345678");
		submit.click();
		this.webClient.waitForBackgroundJavaScript(10000);
	}

	private HtmlForm findForm(HtmlPage login) {
		for (HtmlForm form : login.getForms()) {
			try {
				if (form.getId().equals("form19")) {
					return form;
				}
			}
			catch (ElementNotFoundException ex) {
				// Continue
			}
		}
		throw new IllegalStateException("Could not resolve login form");
	}

	@TestConfiguration
	static class WebClientConfiguration {

		@Bean
		public MockMvcWebClientBuilder mockMvcWebClientBuilder(MockMvc mockMvc, Environment environment) {
			return MockMvcWebClientBuilder.mockMvcSetup(mockMvc).withDelegate(new InternetExplorerLocalHostWebClient(environment));
		}

	}

	static class InternetExplorerLocalHostWebClient extends WebClient {

		private final Environment environment;

		public InternetExplorerLocalHostWebClient(Environment environment) {
			super(BrowserVersion.INTERNET_EXPLORER);
			Assert.notNull(environment, "Environment must not be null");
			this.environment = environment;
		}

		@Override
		public <P extends Page> P getPage(String url) throws IOException, FailingHttpStatusCodeException {
			if (url.startsWith("/")) {
				String port = this.environment.getProperty("local.server.port", "8080");
				url = "http://localhost:" + port + url;
			}
			return super.getPage(url);
		}
	}

}
