#!/bin/sh

. sign; cd dist; scp WolvesAndSheep.jar dreitter@cc.ist.psu.edu:was/; cd -
scp src/test-was.sh dreitter@cc.ist.psu.edu:was/
scp src/run.py dreitter@cc.ist.psu.edu:was/
