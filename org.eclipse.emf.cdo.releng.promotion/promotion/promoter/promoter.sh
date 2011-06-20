#!/usr/bin/env bash
set -e

############################################################
# THIS SCRIPT MUST BE EXECUTED IN THE PROJECT CONFIG AREA!!! 
############################################################

promoterInstallArea=`dirname "$0"`
projectConfigArea=`pwd -P`
. "$projectConfigArea/promoter.properties"


tasksDir=$projectWorkingArea/tasks
inprogressDir=$tasksDir.inprogress

##########################################################################################
# Further down the script ensures that this critical section is not executed concurrently.
##########################################################################################

CriticalSection ()
{
	# Check working area for new tasks.
	if [ -d "$tasksDir" ]
	then
		mv -f "$tasksDir" "$inprogressDir"
		tasks=`ls -A "$inprogressDir"`
		if [ "$tasks" ]
		then
			##############
			CheckPromotion
			##############
		fi
	fi

	# Check hudson jobs area for new builds.
	for jobName in `ls "$projectConfigArea/jobs"`
	do
		file=$projectWorkingArea/$jobName.nextBuildNumber
	  if [ -f "$file" ]
	  then
	    lastBuildNumber=`cat "$file"`
	  else
	    lastBuildNumber=1
	  fi
	
	  nextBuildNumber=`cat "$hudsonJobsArea/$jobName/nextBuildNumber"`
	  if [ "$nextBuildNumber" != "$lastBuildNumber" ]
	  then
			##############
	  	CheckPromotion
			##############
	  fi
	done
}


#########################################################
# Execute the critical section if a lock can be acquired.
#########################################################

CheckPromotion ()
{
	promoterAntFile=$projectWorkingArea/promoter.ant
	rm -f "$promoterAntFile"
	
  "$JAVA_HOME/bin/java" -cp "$promoterInstallArea/classes" Main
  
  if [ -f "$promoterAntFile" ]
  then
	  "$ANT_HOME/bin/ant" -f "$promoterAntFile"
	fi

  # Exit when done.
  # Next check will be triggered by cron...
  exit 0
}


#########################################################
# Execute the critical section if a lock can be acquired.
#########################################################

mkdir -p "$projectWorkingArea"
lockFile=$projectWorkingArea/promoter.lock

if ( set -o noclobber; echo "$$" > "$lockFile" ) 2> /dev/null; 
then
  trap 'rm -f "$lockFile"; rm -rf "$inprogressDir"; exit $?' INT TERM EXIT

	###############
	CriticalSection
	###############
		
  rm -f "$lockFile"
  trap - INT TERM EXIT
fi 
