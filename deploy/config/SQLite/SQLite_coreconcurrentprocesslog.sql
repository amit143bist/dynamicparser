CREATE TABLE "coreconcurrentprocesslog" (
	"processid"	TEXT,
	"batchid"	TEXT,
	"processstartdatetime"	INTEGER,
	"processenddatetime"	INTEGER,
	"processstatus"	TEXT,
	"groupid"	TEXT,
	"accountid"	TEXT,
	"userid"	TEXT,
	"totalrecordsinprocess"	INTEGER,
	"createddatetime"	INTEGER,
	"updateddatetime"	INTEGER,
	"createdby"	TEXT,
	"updatedby"	TEXT,
	PRIMARY KEY("processid"),
	FOREIGN KEY("batchid") REFERENCES "corescheduledbatchlog"("batchid")
)