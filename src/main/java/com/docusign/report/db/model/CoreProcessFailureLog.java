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
@Table(name = "coreprocessfailurelog")
public class CoreProcessFailureLog extends AuditData {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2729284565481864993L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "processfailureid")
	String processFailureId;

	@Column(name = "batchid")
	String batchId;

	@Column(name = "failurecode")
	String failureCode;

	@Column(name = "failurereason")
	String failureReason;

	@Column(name = "failuredatetime")
	Long failureDateTime;

	@Column(name = "successdatetime")
	Long successDateTime;

	@Column(name = "failurerecordid")
	String failureRecordId;

	@Column(name = "failurestep")
	String failureStep;

	@Column(name = "retrystatus")
	String retryStatus;

	@Column(name = "retrycount")
	Long retryCount;
}