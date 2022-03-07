#!/bin/bash
# Runs fuzzing experiment using Tmux

set -e

cd $HOME/mu2/

if [ -z "$FUZZ_CONFIG" ]; then
    echo "Please set FUZZ_CONFIG env vars" > /dev/stderr
    exit 1
fi

if [ $FUZZ_CONFIG == "zest" ]; then
    FUZZ_SCRIPT="scripts/runZest.sh"
elif [ $FUZZ_CONFIG == "mu2" ]; then
    cd $HOME/jqf/
    (cd $HOME/jqf; git checkout master; git pull; mvn install > /dev/null && echo "Built: JQF master for Mu2")
    FUZZ_SCRIPT="scripts/runMu2.sh"
elif [ $FUZZ_CONFIG == "mu2nosave" ]; then
    (cd $HOME/jqf; git checkout vasu/mu2_no_save; git pull; mvn install > /dev/null && echo "Built: JQF for Mu2 no save")
    FUZZ_SCRIPT="scripts/runMu2.sh"
fi

echo tmux new-session -d "bash $FUZZ_SCRIPT $FUZZ_CONFIG $FUZZ_ARGS"
bash $FUZZ_SCRIPT $FUZZ_CONFIG $FUZZ_ARGS
