#!/bin/sh
export CLASSPATH=.:dist/*
java  -Xmx1500m -Dfile.encoding=UTF-8 -server -Dwzpath=wz net.server.Server
