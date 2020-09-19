#!/bin/sh

cd `dirname $0`
exec /usr/bin/daemon -o daemon.info -r -n pucum -i -D "$PWD" -- \
 mvn -q -l /dev/stdout compile exec:java
