#! /bin/bash

##
# Script to test ThorlaMain using ThorlaExample
##

JAR=`pwd`/tmp/thorla.jar

sbt proguard
mkdir -p tmp
cp target/scala-2.9.1/thorla_2.9.1-0.1.min.jar $JAR
(cd target/scala-2.9.1/test-classes/; jar uf $JAR test/thorla/ThorlaExample*.class)
scala $JAR list
scala $JAR example:foo
