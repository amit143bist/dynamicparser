CREATE TABLE reportdb.dbo.corescheduledbatchlog (
	batchtype varchar(200) NOT NULL,
	batchstartparameters varchar(500) NOT NULL,
	createdby varchar(100) NOT NULL,
	updatedby varchar(100) NULL,
	totalrecords bigint NULL,
	batchstartdatetime bigint NOT NULL,
	batchenddatetime bigint NULL,
	createddatetime bigint NOT NULL,
	updateddatetime bigint NULL,
	batchid varchar(100) NOT NULL,
	CONSTRAINT corescheduledbatchlog_PK PRIMARY KEY (batchid)
) GO