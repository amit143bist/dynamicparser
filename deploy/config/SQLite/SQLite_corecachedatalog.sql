CREATE TABLE "corecachedatalog" (
	"cacheid"	TEXT,
	"cachekey"	TEXT UNIQUE,
	"cachevalue"	TEXT UNIQUE,
	"cachereference"	TEXT,
	"createddatetime"	INTEGER,
	"updateddatetime"	INTEGER,
	"createdby"	TEXT,
	"updatedby"	TEXT,
	PRIMARY KEY("cacheid")
)