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
@Table(name = "corescheduledbatchlog")
public class CoreScheduledBatchLog extends AuditData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6231169563307669261L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "batchid")
	String batchId;

	@Column(name = "batchtype")
	String batchType;

	@Column(name = "batchstartdatetime")
	Long batchStartDateTime;

	@Column(name = "batchenddatetime")
	Long batchEndDateTime;

	@Column(name = "batchstartparameters")
	String batchStartParameters;

	@Column(name = "totalrecords")
	Long totalRecords;
}