#!/bin/sh

. sign; cd dist; scp WolvesAndSheep.jar dreitter@brain.ist.psu.edu:was/; cd -
scp src/test-was.sh dreitter@brain.ist.psu.edu:was/
scp src/graphs.R dreitter@brain.ist.psu.edu:was/
scp src/run.py dreitter@brain.ist.psu.edu:was/
