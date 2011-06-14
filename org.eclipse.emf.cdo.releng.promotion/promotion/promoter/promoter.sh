#!/usr/bin/env bash
set -e

############################################################
# THIS SCRIPT MUST BE EXECUTED IN THE PROJECT CONFIG AREA!!! 
############################################################

promoterInstallArea=`dirname "$0"`
projectConfigArea=`pwd -P`
. "$projectConfigArea/promoter.properties"

mkdir -pv "$projectWorkingArea"

##########################################################################################
# Further down the script ensures that this critical section is not executed concurrently.
##########################################################################################

CriticalSection ()
{
	rm -rf "$projectDownloadsArea/temp"
	
	localJobsDir=$projectConfigArea/jobs
	for jobName in `ls "$localJobsDir"`
	do
		file=$projectWorkingArea/$jobName.nextBuildNumber
		
	  if [ -f "$file" ]
	  then
	    lastBuildNumber=`cat "$file"`
	  else
	    lastBuildNumber=1
	  fi
	
	  nextBuildNumber=`cat "$JOBS_HOME/$jobName/nextBuildNumber"`
	  if [ "$nextBuildNumber" != "$lastBuildNumber" ]
	  then
	    echo "Checking $jobName for builds that need promotion..."
	    "$JAVA_HOME/bin/java" -cp "$promoterInstallArea/classes" Checker "$projectDownloadsArea" "$JOBS_HOME" "$jobName" "$lastBuildNumber" "$nextBuildNumber"
	    exit 0
	  fi
	done
}

#########################################################
# Execute the critical section if a lock can be acquired.
#########################################################

lockFile=$projectWorkingArea/promoter.lock

if ( set -o noclobber; echo "$$" > "$lockFile" ) 2> /dev/null; 
then
  trap 'rm -f "$lockFile"; exit $?' INT TERM EXIT

	###############
	CriticalSection
	###############
		
  rm -f "$lockFile"
  trap - INT TERM EXIT
else
	echo "Promoter already being executed by process $(cat $lockFile)."
fi 

