#!/bin/bash

# This checks all the players in players/*.jar and removes unnecessary packages.
#
#


target=dist/WolvesAndSheep.jar

alljars=
allclasses=
for file in `ls players/*.jar`; do

file2=`echo $file | tr [:upper:] [:lower:]` 
mv $file $file2 
file=$file2

# delete javafx and was packages if necessary
zip -d $file javafx/*
zip -d $file was/*
zip -d $file players/*
zip -d $file basic/*
zip -d $file sandbox2.policy test-was.sh reitter.jar

if [ "$file" != "players/reitter.jar" ]; then
   zip -d $file reitter/*
fi

allclasses="$allclasses `basename $file .jar`"
alljars="$alljars:$file"

done

#jar file must be named after last name.

# compile
rm $target
ant

# if jar exists...
if [ -e $target ]; then

    echo $alljars

fi

# sign jar file

jarsigner -storepass javakey  -keypass javakey dist/WolvesAndSheep.jar dr


echo "Suggsted run command:"
echo "./java17 -Xmx1500m -classpath dist/WolvesAndSheep.jar:$alljars was.Tournament -e -c -ist240 -r 3"
