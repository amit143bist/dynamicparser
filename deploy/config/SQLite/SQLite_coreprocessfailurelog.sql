CREATE TABLE "coreprocessfailurelog" (
	"processfailureid"	TEXT,
	"batchid"	TEXT,
	"failurecode"	TEXT,
	"failurereason"	TEXT,
	"failuredatetime"	INTEGER,
	"successdatetime"	INTEGER,
	"failurerecordid"	TEXT,
	"failurestep"	TEXT,
	"retrystatus"	TEXT,
	"retrycount"	INTEGER,
	"createddatetime"	INTEGER,
	"updateddatetime"	INTEGER,
	"createdby"	TEXT,
	"updatedby"	TEXT,
	PRIMARY KEY("processfailureid")
)