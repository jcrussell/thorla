#! /bin/bash

##
# Script to test ThorlaMain using ThorlaExample
##

JAR=`pwd`/tmp/thorla.jar

if [ -f $JAR ]; then
  (cd target/scala-2.9.1/classes/; jar uf $JAR thorla/*.class)
else
  sbt proguard
  mkdir -p tmp
  cp target/scala-2.9.1/thorla_2.9.1-0.1.min.jar $JAR
fi

(cd target/scala-2.9.1/test-classes/; jar uf $JAR test/thorla/ThorlaExample*.class)
#scala $JAR list
scala $JAR example:foo
scala $JAR example:with_int_arg 10
scala $JAR example:with_options
