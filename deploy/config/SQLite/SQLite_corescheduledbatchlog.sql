CREATE TABLE "corescheduledbatchlog" (
	"batchid"	TEXT,
	"batchtype"	TEXT,
	"batchstartdatetime"	INTEGER,
	"batchenddatetime"	INTEGER,
	"batchstartparameters"	TEXT,
	"totalrecords"	INTEGER,
	"createddatetime"	INTEGER,
	"updateddatetime"	INTEGER,
	"createdby"	TEXT,
	"updatedby"	TEXT,
	PRIMARY KEY("batchid")
)