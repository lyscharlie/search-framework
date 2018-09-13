#!/bin/ksh
cd ..
mvn clean package -Dmaven.test.skip 
cd -
