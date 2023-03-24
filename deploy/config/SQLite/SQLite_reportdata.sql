CREATE TABLE "reportdata" (
	"recordid"	TEXT NOT NULL,
	"createddatetime"	INTEGER NOT NULL,
	"createdby"	NUMERIC NOT NULL,
	"accountid"	TEXT NOT NULL,
	"batchid"	TEXT NOT NULL,
	"processid"	TEXT NOT NULL,
	"envelopeid"	TEXT NOT NULL,
	"sendername"	TEXT NOT NULL,
	"senderemail"	TEXT NOT NULL,
	"sentdate"	INTEGER NOT NULL,
	"recipientcount"	INTEGER NOT NULL,
	"documentcount"	INTEGER NOT NULL,
	"pagescount"	INTEGER NOT NULL,
	"status"	TEXT NOT NULL,
	"completeddate"	INTEGER,
	"billable"	TEXT,
	"completionrate"	INTEGER,
	"completionvelocity"	TEXT,
	"greenimpact"	INTEGER,
	"ecf"	TEXT,
	"employeeid"	TEXT,
	PRIMARY KEY("rowid")
)