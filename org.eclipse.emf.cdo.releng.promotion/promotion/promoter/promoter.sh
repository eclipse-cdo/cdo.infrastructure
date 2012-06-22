#!/usr/bin/env bash
set -e

#echo "Promotion failed: http://www.eclipse.org/cdo/downloads" | mail -s "Promotion Failed" -a /tmp/promotion.emf.cdo/promoter.ant -a ~/promotion.log stepper@esc-net.de

############################################################
# THIS SCRIPT MUST BE EXECUTED IN THE PROJECT CONFIG AREA!!!
############################################################

promoterInstallArea=`dirname "$0"`
configArea=`pwd -P`
. "$configArea/promoter.properties"


tasksDir=$workingArea/public/tasks
inprogressDir=$tasksDir.inprogress
args=$@

##########################################################################################
# Further down the script ensures that this critical section is not executed concurrently.
##########################################################################################

CriticalSection ()
{
	if echo "$args" | grep -q -e '--force'
	then
		##############
		CheckPromotion
		##############
	fi

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
	compositionTempFolder=$DOWNLOADS_HOME/$downloadsPath/$compositionTempPath
	rm -rf "$compositionTempFolder"

	classPath="$promoterInstallArea/classes"
  if [ -n "$extraClassPath" ]
	then
  	classPath="$classPath:$extraClassPath"
	fi

  if [ -z "$classPromoter" ]
	then
  	classPromoter=promoter.Promoter
	fi

	############################################################################################################
  "$JAVA_HOME/bin/java" "-DpromoterInstallArea=$promoterInstallArea" -cp "$classPath" "$classPromoter" "$args"
	############################################################################################################

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
touch "$workingArea/touchpoint.start"

lockFile=$workingArea/promoter.lock
if ( set -o noclobber; echo "$$" > "$lockFile" ) 2> /dev/null;
then
  trap 'touch "$workingArea/touchpoint.finish"; rm -f "$lockFile"; rm -rf "$inprogressDir"; exit $?' INT TERM EXIT

	###############
	CriticalSection
	###############

  touch "$workingArea/touchpoint.finish"
  rm -f "$lockFile"
  trap - INT TERM EXIT
fi
