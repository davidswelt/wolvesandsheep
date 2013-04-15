#!/usr/bin/python

targetfile = "/home/dreitter/submission/media/results/latest.html"
tourn_args = "-c -t -e -q -r 48"

import os

os.system("rm -r players >/dev/null; mkdir players; cp ../submission/media/was/*.jar players/");

# list of players

jars = ['basic.Wolf','basic.Sheep']

for dir in ["players","classic.players"]:
    for r,d,f in os.walk(dir):
    	for files in f:
            if files.endswith(".jar"):
	        jars += [files[:-4]]


import datetime

tmpfile = targetfile+".tmp"

def now ():
    return datetime.datetime.now().strftime("%Y-%m-%d %H:%M")
prefix = "{% extends \"base_generic.html\" %}{% block content %}<h1>Tournament results</h1><i>Time: "+now()+"</i><p><pre>"

cmd = "java -cp WolvesAndSheep.jar:lib/\*:players/\*:classic.players/\*:../was/\* was.Tournament %s %s >>%s" % (tourn_args, " ".join(jars), tmpfile)

print cmd

with open(tmpfile, "w") as text_file:
    text_file.write(prefix)
os.system(cmd)
with open(tmpfile, "a") as text_file:
    text_file.write("</pre>Finished:"+now()+".{% endblock content %}")

os.system("mv \"%s\" \"%s\""%(tmpfile,targetfile))
