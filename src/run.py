#!/usr/bin/python

import random

targetfile = "/home/dreitter/submission/media/results/latest.html"
tmpf = "%s"%random.randint(1, 10)
ist240 = True # run actual 240 tournament
tourn_args = "-d 10 -c -e"  # run for 10 minutes
#tourn_args = "-c -t -e -q -r 8"

import os

os.system("rm -r players >/dev/null; mkdir players; cp ../submission/media/was/*.jar players/");


if ist240:
    tourn_args = "-ist240 " + tourn_args
    jars = []
else:


    # list of players

    jars = ['basic.Wolf','basic.Sheep']

    for dir in ["players","classic.players"]:
        for r,d,f in os.walk(dir):
            for files in f:
                if files.endswith(".jar"):
                    jars += [files[:-4]]


import datetime

tmpfile = targetfile+"."+tmpf

def now ():
    return datetime.datetime.now().strftime("%Y-%m-%d %H:%M")
prefix = "{% extends \"base_generic.html\" %}{% block content %}<h1>Tournament results</h1><i>Time: "+now()+"</i><p><pre>"

cmdtmpfile = tmpfile+".run"
runclass = "was.IST240Tournament" if ist240 else: "was.Tournament"

cmd = "java -cp WolvesAndSheep.jar:lib/\*:players/\*:classic.players/\*:../was/\* %s %s %s >>%s" % (runclass, tourn_args, " ".join(jars), cmdtmpfile)

print cmd

with open(tmpfile, "w") as text_file:
    text_file.write(prefix)
os.system(cmd)

if ist240:
    # insert portion after marker only
    os.system("grep -A1000 -e '##########' \"%s\" >>\"%s\""%(cmdtmpfile,tmpfile))
else:
    os.system("cat \"%s\" >>\"%s\""%(cmdtmpfile,tmpfile))

with open(tmpfile, "a") as text_file:
    text_file.write("</pre>Finished:"+now()+".{% endblock content %}")

os.system("mv \"%s\" \"%s\""%(tmpfile,targetfile))
