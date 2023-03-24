package com.docusign.report;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AuditorAware;

@Configuration
@Profile({ "unittest" })
public class TestAuditConfiguration {

	@Bean
	public AuditorAware<String> auditorTestProvider() {
		return new TestAuditorAwareImpl();
	}
}

class TestAuditorAwareImpl implements AuditorAware<String> {

	@Override
	public Optional<String> getCurrentAuditor() {

		return Optional.of("Config Setup");
	}
}