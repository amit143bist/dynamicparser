package com.docusign.report;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AuditorAware;

@Configuration
@Profile({ "!unittest" })
public class AuditConfiguration {

	@Bean
	public AuditorAware<String> auditorProvider() {
		return new AuditorAwareImpl();
	}

}

class AuditorAwareImpl implements AuditorAware<String> {

	@Value("${app.db.auditorname}")
	private String auditorName;

	@Override
	public Optional<String> getCurrentAuditor() {

		return Optional.of(auditorName);
	}
}