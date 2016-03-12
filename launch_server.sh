#!/bin/sh
export CLASSPATH=.:dist/*
cp  ~/Desktop/twms113/dist/twms113.jar ./dist/
java  -Xmx1500m -Dfile.encoding=UTF-8 -server -Dwzpath=wz/ net.server.Server
