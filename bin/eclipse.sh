#!/bin/ksh
cd ..
mvn eclipse:clean eclipse:eclipse  -DdownloadSources=true -Dmaven.test.skip=true -U
cd -