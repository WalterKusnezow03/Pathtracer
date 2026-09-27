#!/bin/bash

cd VCG_Raytracer || exit 1

# alte class files löschen
find . -name "*.class" -delete

# kompilieren in out/
javac -d out $(find src -name "*.java")

# starten
java -cp out Main




### add as runnable bin
##chmod +x run.sh  


##execute with
##./run.sh