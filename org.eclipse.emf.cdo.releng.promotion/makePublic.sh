#!/usr/bin/env bash

. git/org.eclipse.emf.cdo.releng.promotion/config/promoter.properties
compositionFolder=$DOWNLOADS_HOME/$projectPath/$compositionPath
compositionTempFolder=$DOWNLOADS_HOME/$projectPath/$compositionTempPath

if [ -d "$compositionTempFolder" ]
then
	tmpFolder=$compositionFolder.tmp
  rm -rf "$tmpFolder"

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
