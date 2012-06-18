#!/usr/bin/env bash

cd ~

rm -rf cdo.infrastructure
git clone file:///gitroot/cdo/cdo.infrastructure.git

rm -rf promotion
cp -a cdo.infrastructure/org.eclipse.emf.cdo.releng.promotion/promotion promotion

chmod u+x promotion/*.sh promotion/promoter/*.sh

