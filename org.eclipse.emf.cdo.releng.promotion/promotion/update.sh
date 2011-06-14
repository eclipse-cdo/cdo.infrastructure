#!/usr/bin/env bash

#################################
# Initially on build.eclipse.org:
# cd ~; rm -rf promotion; svn checkout https://dev.eclipse.org/svnroot/modeling/org.eclipse.emf.cdo/infrastructure/org.eclipse.emf.cdo.releng.promotion/promotion; cd promotion
#################################

svn update
chmod -v u+x *.sh
chmod -v u+x promoter/*.sh

cd config
../promoter/reset.sh
