package com.docusign.report.db.model.naming;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class UpperCaseNamingStrategyStandardImpl implements PhysicalNamingStrategy {

	public static final UpperCaseNamingStrategyStandardImpl INSTANCE = new UpperCaseNamingStrategyStandardImpl();

	@Override
	public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {

		return new Identifier(name.getText().toUpperCase(), Identifier.isQuoted(name.getText()));
	}

	@Override
	public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {

		return new Identifier(name.getText().toUpperCase(), Identifier.isQuoted(name.getText()));
	}

	@Override
	public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {

		return new Identifier(name.getText().toUpperCase(), Identifier.isQuoted(name.getText()));
	}

	@Override
	public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {

		return new Identifier(name.getText().toUpperCase(), Identifier.isQuoted(name.getText()));
	}

	@Override
	public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {

		return new Identifier(name.getText().toUpperCase(), Identifier.isQuoted(name.getText()));
	}

}