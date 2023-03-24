INSERT INTO corescheduledbatchlog(
	batchid, batchtype, batchstartdatetime, batchenddatetime, batchstartparameters, createddatetime, updateddatetime, createdby, updatedby, totalrecords)
	VALUES ('b4ad9898-dd2f-43d4-b685-dd08aebc5063', 'envelopepull', 1580592540, null, 'Start Params', 1580592540, null, 'TestScript', null, 20);

INSERT INTO corescheduledbatchlog(
	batchid, batchtype, batchstartdatetime, batchenddatetime, batchstartparameters, createddatetime, updateddatetime, createdby, updatedby, totalrecords)
	VALUES ('b4ad9898-dd2f-43d4-b685-dd08aebc5064', 'envelopepull', 1580592540, null, 'Start Params', 1580592540, null, 'TestScript', null, 20);

INSERT INTO corescheduledbatchlog(
	batchid, batchtype, batchstartdatetime, batchenddatetime, batchstartparameters, createddatetime, updateddatetime, createdby, updatedby, totalrecords)
	VALUES ('b4ad9898-dd2f-43d4-b685-dd08aebc5065', 'envelopepull', 1580592540, null, 'Start Params', 1580592540, null, 'TestScript', null, 20);

INSERT INTO coreconcurrentprocesslog(
	processid, batchid, processstartdatetime, processenddatetime, processstatus, totalrecordsinprocess, createddatetime, updateddatetime, createdby, updatedby)
	VALUES ('84a3a1d3-02e0-4ca5-a5bc-590f37e0835e', 'b4ad9898-dd2f-43d4-b685-dd08aebc5065', 1580592540, null, 'INPROGRESS', 50, 1580592540, null, 'TestScript', null);
	
INSERT INTO coreconcurrentprocesslog(
	processid, batchid, processstartdatetime, processenddatetime, processstatus, totalrecordsinprocess, createddatetime, updateddatetime, createdby, updatedby)
	VALUES ('84a3a1d3-02e0-4ca5-a5bc-590f37e0836e', 'b4ad9898-dd2f-43d4-b685-dd08aebc5065', 1580592540, null, 'INPROGRESS', 50, 1580592540, null, 'TestScript', null);
	
INSERT INTO corescheduledbatchlog(
	batchid, batchtype, batchstartdatetime, batchenddatetime, batchstartparameters, createddatetime, updateddatetime, createdby, updatedby, totalrecords)
	VALUES ('b4ad9898-dd2f-43d4-b685-dd08aebc5067', 'envelopepull', 1580592540, null, 'Start Params', 1580592540, null, 'TestScript', null, 20);

INSERT INTO coreconcurrentprocesslog(
	processid, batchid, processstartdatetime, processenddatetime, processstatus, totalrecordsinprocess, createddatetime, updateddatetime, createdby, updatedby)
	VALUES ('d361a0aa-6b09-4fd8-861d-aafce14f7e14', 'b4ad9898-dd2f-43d4-b685-dd08aebc5067', 1580592540, 1583098140, 'COMPLETED', 50, 1580592540, null, 'TestScript', null);
	
INSERT INTO coreprocessfailurelog(
	processfailureid, batchid, failurecode, failurereason, failuredatetime, failurerecordid, failurestep, createddatetime, updateddatetime, createdby, updatedby, retrystatus, retrycount)
	VALUES ('f228254a-6f82-4ec1-9c8a-44a1a20577f5', 'b4ad9898-dd2f-43d4-b685-dd08aebc5065', 'ERROR_11', 'API Error', 1580592540, '1234', 'CALLING_DS_API', 1580592540, null, 'TestScript', null, 'F', null);	

INSERT INTO coreprocessfailurelog(
	processfailureid, batchid, failurecode, failurereason, failuredatetime, failurerecordid, failurestep, createddatetime, updateddatetime, createdby, updatedby, retrystatus, retrycount)
	VALUES ('f228254a-6f82-4ec1-9c8a-44a1a20577f6', 'b4ad9898-dd2f-43d4-b685-dd08aebc5065', 'ERROR_12', 'Service Error', 1580592540, '1234', 'CALLING_DS_API', 1580592540, null, 'TestScript', null, 'T', null);
	
INSERT INTO coreprocessfailurelog(
	processfailureid, batchid, failurecode, failurereason, failuredatetime, failurerecordid, failurestep, createddatetime, updateddatetime, createdby, updatedby, retrystatus, retrycount)
	VALUES ('f228254a-6f82-4ec1-9c8a-44a1a20577f7', 'b4ad9898-dd2f-43d4-b685-dd08aebc5065', 'ERROR_12', 'Service Error', 1580592540, '1234', 'CALLING_DS_API', 1580592540, null, 'TestScript', null, null, null);	