@echo off
@title MoopleDEV Server v83
set CLASSPATH=.;dist\*
java -Xmx300m -Dwzpath=wz\ net.server.Server
pause