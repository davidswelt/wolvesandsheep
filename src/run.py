#!/usr/bin/python

import random

assignment_name = 'wasJAR'
homepath = '/Users/dreitter'
targetfile = "/Users/dreitter/submission/media/results/latest.html"
tmpf = "%s"%random.randint(1, 10000)
ist240 = True # run actual 240 tournament
tourn_args = "-d 45 -j 4 -c log/log.tsv"  # run for 45 minutes, four cores
#tourn_args = "-q -r 8"

playerspath = "players/"

import os
import zipfile
import re


os.system("rm -r players >/dev/null; mkdir players");
os.system("scp -p dreitter@cc.ist.psu.edu:/home/dreitter/submission/media/%s/*.jar %s"%(assignment_name,playerspath))
os.system("./test-was.sh")
if ist240:
    tourn_args = "" + tourn_args
    jars = []
else:
    jars = ['basic.Wolf','basic.Sheep']


def readPackageNames(d,f):
    

    zip=zipfile.ZipFile(d+'/'+f)
    l = zip.namelist()
    regex = re.compile(r'^([a-zA-Z0-9_]+)/(Sheep|Wolf)\.(class|java)$')
    packages = [m.group(1) for m in [regex.match(lib) for lib in l] if m] 
    return packages

# list of players


packages = set()
for dir in ["players","classic.players"]:
    for r,d,f in os.walk(dir):
        for files in f:
            if not ist240 and files.endswith(".jar"):
                jars += [files[:-4]]
            packages = packages.union(readPackageNames(dir,files))

print packages

import datetime

tmpfile = targetfile+"."+tmpf

def now ():
    return datetime.datetime.now().strftime("%Y-%m-%d %H:%M")
prefix = "{% extends \"base_generic.html\" %}{% block content %}<h1>Tournament results</h1><i>Time: "+now()+"</i><p><pre>"

cmdtmpfile = tmpfile+".run"
runclass = "was.ClassTournament" if ist240 else "was.Tournament"

cmd = "java -cp WolvesAndSheep.jar:lib/\*:players/\*:classic.players/\*:../was/\* %s %s %s >%s" % (runclass, tourn_args, " ".join(jars), cmdtmpfile)

print cmd


os.system(cmd)

with open(tmpfile, "w") as text_file:
    text_file.write(prefix)

if ist240:
    # insert portion after marker only
    os.system("grep -A1000 -e '##########' \"%s\" >>\"%s\""%(cmdtmpfile,tmpfile))
else:
    os.system("cat \"%s\" >>\"%s\""%(cmdtmpfile,tmpfile))

    
import os.path,time
import datetime
with open(tmpfile, "a") as text_file:
    
    print  >>text_file, "<h3>File versions</h3>"
    print  >>text_file, "<table border=0 cellspacing=10>"

    l = os.listdir(playerspath)
    l.sort(cmp)
    for infile in l:
   
        t = os.path.getmtime(playerspath+infile)
        print >>text_file, ("<tr><td>%s</td><td>%s</td></tr>"%(infile,datetime.datetime.fromtimestamp(t)))

    print  >>text_file, "</table>"

    text_file.write("</pre>Finished:"+now()+".{% endblock content %}")


os.system("R --vanilla < graphs.R")


os.system("mv \"%s\" \"%s\""%(tmpfile,targetfile))
os.system("rm \"%s\""%(cmdtmpfile))
os.system("scp \"%s\" log/*.png dreitter@cc.ist.psu.edu:/home/dreitter/submission/media/results/"%(targetfile))