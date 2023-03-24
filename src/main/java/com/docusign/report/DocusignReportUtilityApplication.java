package com.docusign.report;

import javax.script.ScriptEngineManager;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import com.docusign.report.shell.DocuSignInputReader;
import com.docusign.report.shell.DocuSignShellHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(scanBasePackages = { "com.docusign.report" })
@EnableCaching
@EnableScheduling // This enables scheduling to clear the cache
@EnableTransactionManagement
@EnableJpaAuditing
@EnableSwagger2
public class DocusignReportUtilityApplication {

	@Value("${app.async.queuecapacity}")
	private int queueCapacity;

	@Value("${app.async.maxpoolsize}")
	private int maxPoolSize;

	@Value("${app.async.corepoolsize}")
	private int corePoolSize;

	@Value("${app.async.executornameprefix}")
	private String executorNamePrefix;

	@Value("${app.authorization.token.cacheExpirationSeconds}")
	private String cacheExpirationSeconds;

	public static void main(String[] args) {

		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(DocusignReportUtilityApplication.class)
				.bannerMode(Banner.Mode.CONSOLE).web(WebApplicationType.NONE).build().run(args);

		SpringApplication.exit(ctx, () -> 0);
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public ScriptEngineManager scriptEngineManager() {

		return new ScriptEngineManager();
	}

	@Bean
	CacheManager cacheManager() {
		return new ConcurrentMapCacheManager("token", "baseUrl");
	}

	@Bean
	public String getScheduleFixedRate() {
		return Long.toString(Long.valueOf(cacheExpirationSeconds) * 1000);
	}

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.any()).build();

	}

	@Bean(name = "recordTaskExecutor")
	public TaskExecutor recordTaskExecutor() {

		final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(corePoolSize);
		executor.setMaxPoolSize(maxPoolSize);
		executor.setQueueCapacity(queueCapacity);
		executor.setThreadNamePrefix(executorNamePrefix);
		return executor;
	}

	@Bean
	public DocuSignInputReader inputReader(@Lazy LineReader lineReader, DocuSignShellHelper shellHelper) {
		return new DocuSignInputReader(lineReader, shellHelper);
	}

	@Bean
	public DocuSignShellHelper shellHelper(@Lazy Terminal terminal) {
		return new DocuSignShellHelper(terminal);
	}

}