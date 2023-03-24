CREATE TABLE reportdb.dbo.coreconcurrentprocesslog (
	processstatus varchar(100) NOT NULL,
	totalrecordsinprocess bigint NOT NULL,
	createdby varchar(100) NOT NULL,
	updatedby varchar(100) NULL,
	processstartdatetime bigint NOT NULL,
	processenddatetime bigint NULL,
	createddatetime bigint NOT NULL,
	updateddatetime bigint NULL,
	processid varchar(100) NOT NULL,
	batchid varchar(100) NOT NULL,
	groupid varchar(100) NULL,
	accountid varchar(100) NULL,
	userid varchar(100) NULL,
	CONSTRAINT coreconcurrentprocesslog_PK PRIMARY KEY (processid)
) GO