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
@Table(name = "coreconcurrentprocesslog")
public class CoreConcurrentProcessLog extends AuditData {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4628517513041385132L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "processid")
	String processId;

	@Column(name = "batchid")
	String batchId;

	@Column(name = "processstartdatetime")
	Long processStartDateTime;

	@Column(name = "processenddatetime")
	Long processEndDateTime;

	@Column(name = "processstatus")
	String processStatus;

	@Column(name = "groupid")
	String groupId;

	@Column(name = "accountid")
	String accountId;

	@Column(name = "userid")
	String userId;

	@Column(name = "totalrecordsinprocess")
	Long totalRecordsInProcess;
}