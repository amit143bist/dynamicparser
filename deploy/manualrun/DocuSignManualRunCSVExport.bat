@echo off
set _JAVA_OPTIONS=-Xms512m -Xmx2048m -XX:NewSize=64m -XX:MaxNewSize=128m -XX:MaxMetaspaceSize=128m -XX:CompressedClassSpaceSize=256m -Xss512k -XX:InitialCodeCacheSize=128m -XX:ReservedCodeCacheSize=256m -XX:MaxDirectMemorySize=256m -XX:-TieredCompilation -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -XX:OnOutOfMemoryError="shutdown -r" -XX:+UseGCOverheadLimit
set logFileMaxSize="500MB"
set utilityPath="C:/cbs/alexion/dynamitex"
set proxyHost="[proxyHost]"
set proxyPort="[proxyPort]"
set springApplicationName="ManualRunCSVExportUtility"

rem triggerjob batchStartDateTime 2020-01-10 batchEndDateTime 2021-06-30 jobType PREPAREDATA
rem triggerjob batchStartDateTime 2020-01-10 batchEndDateTime 2021-06-30 jobType MANAGEDATA
rem mergefiles mergeFileNameFullPath C:\\cbs\\nysdol\\Sample-Bulk-Recipient_Merge.csv inputFilesDir C:\\cbs\\nysdol\\original
rem removeduplicates sourceFileFullPath C:\\cbs\\nysdol\\2020-06-10_2020-06-SomeRecordsRemoved.csv sourceFileColumnIndex 4,0 mergedFileFullPath C:\\cbs\\nysdol\\Sample-Bulk-Recipient_Merge.csv mergedFileColumnIndex 0,1 newFileFullPath C:\\cbs\\nysdol\\NextToProcess.csv
rem removeduplicatesmap sourceFileFullPath C:\\cbs\\nysdol\\2020-06-10_2020-06-SomeRecordsRemoved.csv sourceFileColumnIndex 4,0 mergedFileFullPath C:\\cbs\\nysdol\\Sample-Bulk-Recipient_Merge.csv mergedFileColumnIndex 0,1 newFileFullPath C:\\cbs\\nysdol\\NextToProcess.csv
rem splitfiles startFileNameFullPath C:\\cbs\\nysdol\\NextToProcess.csv splitFilePrefix retry_missing_files totalLinesPerFile 2


echo Starting the %springApplicationName% service

java -jar -Dloader.path=%utilityPath%/lib -Dlogging.level.com.docusign.report.service=DEBUG -Dspring.config.location=file:///%utilityPath%/config/application.yml -DconfigPath=%utilityPath%/config -Dlogging.file=%utilityPath%/manualrun/logs/dsreportutility.log -Dlogging.file.max-size=%logFileMaxSize% C:/cbs/docusignreportutility/target/docusignreportutility-1.0.0-SNAPSHOT.jar
