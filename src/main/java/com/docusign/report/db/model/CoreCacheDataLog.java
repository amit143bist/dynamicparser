package com.docusign.report.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "corecachedatalog")
public class CoreCacheDataLog extends AuditData {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3935724266401440286L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "cacheid")
	String cacheId;

	@Column(name = "cachekey")
	String cacheKey;

	@Column(name = "cachevalue")
	String cacheValue;

	@Column(name = "cachereference")
	String cacheReference;
}