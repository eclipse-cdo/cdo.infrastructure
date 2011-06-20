#!/usr/bin/env bash
set -e

############################################################
# THIS SCRIPT MUST BE EXECUTED IN THE PROJECT CONFIG AREA!!! 
############################################################

promoterInstallArea=`dirname "$0"`
projectConfigArea=`pwd -P`
. "$projectConfigArea/promoter.properties"

rm -rf "$workingArea"
