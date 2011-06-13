#!/usr/bin/env bash
set -e

promotionWorkDir=~/promotion

DOWNLOADS_DIR=/home/data/httpd/download.eclipse.org/modeling/emf/cdo
HUDSON_JOBS_DIR=/shared/jobs
JAVA_HOME=/shared/common/jdk-1.6.0_10
JAVA=$JAVA_HOME/bin/java
ANT=/shared/common/apache-ant-1.7.1/bin/ant

CriticalSection ()
{
	rm -rf "$DOWNLOADS_DIR/temp"
	localJobsDir=$promotionWorkDir/jobs
	
	for jobName in `ls "$localJobsDir"`
	do
		jobDir=$localJobsDir/$jobName
		file=$jobDir/nextBuildNumber
		
	  if [ -f "$file" ]
	  then
	    lastBuildNumber=`cat "$file"`
	  else
	    lastBuildNumber=1
	  fi
	
	  nextBuildNumber=`cat "$HUDSON_JOBS_DIR/$jobName/nextBuildNumber"`
	  if [ "$nextBuildNumber" != "$lastBuildNumber" ]
	  then
	    echo "Checking $jobName for builds that need promotion..."
	    "$JAVA" -cp classes org.eclipse.emf.cdo.releng.promotion.Checker \ 
	    	"$DOWNLOADS_DIR" \
	    	"$HUDSON_JOBS_DIR" \
	    	"$jobName" \
	    	"$lastBuildNumber" \
	    	"$nextBuildNumber"
	    	
	    #"$ANT" -f "$promotionWorkDir/promoter.ant" \
	    #	"-DdownloadsDir=$DOWNLOADS_DIR" \
	    #	"-DhudsonJobsDir=$HUDSON_JOBS_DIR" \
	    #	"-DpromotionWorkDir=$promotionWorkDir" \
	    #	"-DjobName=$jobName" \
	    #	"-DlastBuildNumber=$lastBuildNumber" \
	    #	"-DnextBuildNumber=$nextBuildNumber"
	  fi
	done
}

lockFile=$promotionWorkDir/promote.lock
if ( set -o noclobber; echo "$$" > "$lockFile" ) 2> /dev/null; 
then
  trap 'rm -f "$lockFile"; exit $?' INT TERM EXIT

	###############
	CriticalSection
	###############
		
  rm -f "$lockFile"
  trap - INT TERM EXIT
else
	echo "Promotion already being executed by process $(cat $lockFile)."
fi 

