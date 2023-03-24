-- Drop table

-- DROP TABLE reportdb.dbo.corecachedatalog GO

CREATE TABLE reportdb.dbo.corecachedatalog (
	cachekey varchar(1000) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	cachevalue varchar(1000) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	cachereference varchar(1000) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	createddatetime bigint NOT NULL,
	updateddatetime bigint NULL,
	createdby varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	updatedby varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	cacheid varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	CONSTRAINT corecachedatalog_PK PRIMARY KEY (cacheid)
) GO