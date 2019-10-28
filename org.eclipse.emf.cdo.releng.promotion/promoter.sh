#!/usr/bin/env bash

. git/org.eclipse.emf.cdo.releng.promotion/config/promoter.properties
compositionFolder=$DOWNLOADS_HOME/$downloadsPath/$compositionPath
compositionTempFolder=$DOWNLOADS_HOME/$downloadsPath/$compositionTempPath

if [ -d "$compositionTempFolder" ]
then
	tmpFolder=$compositionFolder.tmp
  rm -rf "$compositionTempFolder"

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
