package com.docusign.report.shell;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class DocuSignPromptProvider implements PromptProvider {

	@Override
	public AttributedString getPrompt() {
		return new AttributedString("DS-SHELL:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE));
	}

}