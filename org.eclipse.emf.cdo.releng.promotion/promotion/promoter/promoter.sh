#!/usr/bin/env bash
set -e

############################################################
# THIS SCRIPT MUST BE EXECUTED IN THE PROJECT CONFIG AREA!!! 
############################################################

promoterInstallArea=`dirname "$0"`
projectConfigArea=`pwd -P`
. "$projectConfigArea/promoter.properties"


##########################################################################################
# Further down the script ensures that this critical section is not executed concurrently.
##########################################################################################

CriticalSection ()
{
	# Check working area for new tasks.

	tasksDir=$projectWorkingArea/tasks
	if [ -d "$tasksDir" ]
	then
		tasks=`ls -A "$tasksDir"`
		if [ "$tasks" ]
		then
			##############
			StartPromotion
			##############
		fi
	fi
	
	# Check hudson jobs area for new builds.

	for jobName in `ls -A "$projectConfigArea/jobs"`
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
			##############
	  	StartPromotion
			##############
	  fi
	done
}


#########################################################
# Execute the critical section if a lock can be acquired.
#########################################################

StartPromotion ()
{
  "$JAVA_HOME/bin/java" -cp "$promoterInstallArea/classes" Checker \
  	"$projectDownloadsArea" \
  	"$JOBS_HOME" \
  	"$jobName" \
  	"$lastBuildNumber"\
  	"$nextBuildNumber"
  	
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
  trap 'rm -f "$lockFile"; exit $?' INT TERM EXIT

	###############
	CriticalSection
	###############
		
  rm -f "$lockFile"
  trap - INT TERM EXIT
else
	echo "Promoter already being executed by process $(cat $lockFile)."
fi 
