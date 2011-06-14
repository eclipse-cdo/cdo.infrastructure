#!/usr/bin/env bash

# Initially on build.eclipse.org:
#svn checkout https://dev.eclipse.org/svnroot/modeling/org.eclipse.emf.cdo/infrastructure/org.eclipse.emf.cdo.releng.promotion/promotion

svn update
chmod -R u+x *.sh

cd config
../promoter/reset.sh
