#!/usr/bin/env bash
set -e

############################################################
# THIS SCRIPT MUST BE EXECUTED IN THE PROJECT CONFIG AREA!!! 
############################################################

promoterInstallArea=`dirname "$0"`
configArea=`pwd -P`
. "$configArea/promoter.properties"


tasksDir=$workingArea/tasks
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
	for jobName in `ls "$configArea/jobs"`
	do
		file=$workingArea/$jobName.nextBuildNumber
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
	#promoterAntFile=$workingArea/promoter.ant
	#rm -f "$promoterAntFile"
	
	compositionTempFolder=$DOWNLOADS_HOME/$downloadsPath/$compositionTempPath
	rm -rf "$compositionTempFolder"
	
  "$JAVA_HOME/bin/java" -cp "$promoterInstallArea/classes" Main
  
  #if [ -f "$promoterAntFile" ]
  #then
  #	echo
	#  "$ANT_HOME/bin/ant" -f "$promoterAntFile"
	#fi

  if [ -d "$compositionTempFolder" ]
  then
		compositionFolder=$DOWNLOADS_HOME/$downloadsPath/$compositionPath
		tmpFolder=$compositionFolder.tmp
		
  	if [ -d "$compositionFolder" ]
	  then
			mv -f "$compositionFolder" "$tmpFolder"
		fi
		
		mv -f "$compositionTempFolder" "$compositionFolder"
		
  	if [ -d "$tmpFolder" ]
	  then
			rm -rf "$tmpFolder"
		fi
	fi
	
  # Exit when done.
  # Next check will be triggered by cron...
  exit 0
}


#########################################################
# Execute the critical section if a lock can be acquired.
#########################################################

mkdir -p "$workingArea"
lockFile=$workingArea/promoter.lock

if ( set -o noclobber; echo "$$" > "$lockFile" ) 2> /dev/null; 
then
  trap 'rm -f "$lockFile"; rm -rf "$inprogressDir"; exit $?' INT TERM EXIT

	###############
	CriticalSection
	###############
		
  rm -f "$lockFile"
  trap - INT TERM EXIT
fi 
