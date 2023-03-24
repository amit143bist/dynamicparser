INSERT INTO corescheduledbatchlog(
	batchid, batchtype, batchstartdatetime, batchenddatetime, batchstartparameters, createddatetime, updateddatetime, createdby, updatedby, totalrecords)
	VALUES ('e781ca58-dec7-44b7-a312-5c21fded402d', 'envelopepull', 1580592540, null, 'Start Params', 1580592540, null, 'TestScript', null, 20);

INSERT INTO corescheduledbatchlog(
	batchid, batchtype, batchstartdatetime, batchenddatetime, batchstartparameters, createddatetime, updateddatetime, createdby, updatedby, totalrecords)
	VALUES ('e781ca58-dec7-44b7-a312-5c21fded402e', 'envelopepull', 1580592540, 1583098140, 'Start Params', 1580592540, 1583098140, 'TestScript', null, 20);

INSERT INTO corescheduledbatchlog(
	batchid, batchtype, batchstartdatetime, batchenddatetime, batchstartparameters, createddatetime, updateddatetime, createdby, updatedby, totalrecords)
	VALUES ('e781ca58-dec7-44b7-a312-5c21fded402f', 'envelopepull', 1583104934, 1583098140, 'Start Params', 1580592540, 1583098140, 'TestScript', null, 20);

INSERT INTO corescheduledbatchlog(
	batchid, batchtype, batchstartdatetime, batchenddatetime, batchstartparameters, createddatetime, updateddatetime, createdby, updatedby, totalrecords)
	VALUES ('d43ac5ef-ebc0-40e9-8122-9574d8641731', 'otherbatchtype', 1580592540, 1583098140, 'Start Params', 1580592540, 1583098140, 'TestScript', null, 20);

INSERT INTO coreconcurrentprocesslog(
	processid, batchid, processstartdatetime, processenddatetime, processstatus, totalrecordsinprocess, createddatetime, updateddatetime, createdby, updatedby)
	VALUES ('84a3a1d3-02e0-4ca5-a5bc-590f37e0834e', 'e781ca58-dec7-44b7-a312-5c21fded402d', 1580592540, null, 'Completed', 500, 1580592540, null, 'TestScript', null);
	
INSERT INTO coreprocessfailurelog(
	processfailureid, batchid, failurecode, failurereason, failuredatetime, failurerecordid, failurestep, createddatetime, updateddatetime, createdby, updatedby, retrystatus, retrycount)
	VALUES ('f228254a-6f82-4ec1-9c8a-44a1a20577f1', 'e781ca58-dec7-44b7-a312-5c21fded402d', 'ERROR_10', 'DB Error', 1580592540, '1234', 'CALLING_DS_API', 1580592540, null, 'TestScript', null, null, null);
	
INSERT INTO coreprocessfailurelog(
	processfailureid, batchid, failurecode, failurereason, failuredatetime, failurerecordid, failurestep, createddatetime, updateddatetime, createdby, updatedby, retrystatus, retrycount)
	VALUES ('f228254a-6f82-4ec1-9c8a-44a1a20577f2', 'e781ca58-dec7-44b7-a312-5c21fded402d', 'ERROR_11', 'API Error', 1580592540, '1234', 'CALLING_DS_API', 1580592540, null, 'TestScript', null, 'F', null);	

INSERT INTO coreprocessfailurelog(
	processfailureid, batchid, failurecode, failurereason, failuredatetime, failurerecordid, failurestep, createddatetime, updateddatetime, createdby, updatedby, retrystatus, retrycount)
	VALUES ('f228254a-6f82-4ec1-9c8a-44a1a20577f3', 'e781ca58-dec7-44b7-a312-5c21fded402d', 'ERROR_12', 'Service Error', 1580592540, '1234', 'CALLING_DS_API', 1580592540, null, 'TestScript', null, 'T', null);		