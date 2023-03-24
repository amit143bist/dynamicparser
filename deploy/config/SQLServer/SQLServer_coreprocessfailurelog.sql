CREATE TABLE reportdb.dbo.coreprocessfailurelog (
	failurecode varchar(100) NOT NULL,
	failurereason varchar(1000) NOT NULL,
	failurerecordid varchar(100) NOT NULL,
	failurestep varchar(100) NOT NULL,
	createdby varchar(100) NOT NULL,
	updatedby varchar(100) NULL,
	retrystatus varchar(100) NULL,
	retrycount bigint NULL,
	failuredatetime bigint NULL,
	successdatetime bigint NULL,
	createddatetime bigint NOT NULL,
	updateddatetime bigint NULL,
	processfailureid varchar(100) NOT NULL,
	batchid varchar(100) NULL,
	CONSTRAINT coreprocessfailurelog_PK PRIMARY KEY (processfailureid)
) GO