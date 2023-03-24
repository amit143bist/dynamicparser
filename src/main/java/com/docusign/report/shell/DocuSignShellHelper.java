package com.docusign.report.shell;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Value;

import com.docusign.report.common.constant.PromptColor;

public class DocuSignShellHelper {

	@Value("${shell.out.info}")
	private String infoColor;

	@Value("${shell.out.error}")
	private String errorColor;

	@Value("${shell.out.success}")
	private String successColor;

	@Value("${shell.out.warning}")
	private String warningColor;

	private Terminal terminal;

	public DocuSignShellHelper(Terminal terminal) {
		this.terminal = terminal;
	}

	public Terminal getTerminal() {
		return terminal;
	}

	public String getColored(String message, PromptColor color) {
		return (new AttributedStringBuilder())
				.append(message, AttributedStyle.DEFAULT.foreground(color.toJlineAttributedStyle())).toAnsi();
	}

	public String getInfoMessage(String message) {
		return getColored(message, PromptColor.valueOf(infoColor));
	}

	public String getSuccessMessage(String message) {
		return getColored(message, PromptColor.valueOf(successColor));
	}

	public String getWarningMessage(String message) {
		return getColored(message, PromptColor.valueOf(warningColor));
	}

	public String getErrorMessage(String message) {
		return getColored(message, PromptColor.valueOf(errorColor));
	}

	/**
	 * Print message to the console in the default color.
	 *
	 * @param message message to print
	 */
	public void print(String message) {
		print(message, null);
	}

	/**
	 * Print message to the console in the success color.
	 *
	 * @param message message to print
	 */
	public void printSuccess(String message) {
		print(message, PromptColor.valueOf(successColor));
	}

	/**
	 * Print message to the console in the info color.
	 *
	 * @param message message to print
	 */
	public void printInfo(String message) {
		print(message, PromptColor.valueOf(infoColor));
	}

	/**
	 * Print message to the console in the warning color.
	 *
	 * @param message message to print
	 */
	public void printWarning(String message) {
		print(message, PromptColor.valueOf(warningColor));
	}

	/**
	 * Print message to the console in the error color.
	 *
	 * @param message message to print
	 */
	public void printError(String message) {
		print(message, PromptColor.valueOf(errorColor));
	}

	/**
	 * Generic Print to the console method.
	 *
	 * @param message message to print
	 * @param color   (optional) prompt color
	 */
	public void print(String message, PromptColor color) {
		String toPrint = message;
		if (color != null) {
			toPrint = getColored(message, color);
		}
		terminal.writer().println(toPrint);
		terminal.flush();
	}

}