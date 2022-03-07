#!/bin/bash

set -e

if [ $# -lt 4 ]; then
	echo "Usage: $0 PEMPATH REMOTE_SERVER FUZZ_CONFIG [FUZZ_ARGS..]" > /dev/stderr
	exit 1
fi

PEMPATH="$1" # example: ~/aws_keys/key.pem
REMOTE="$2" # example: vasumv@0.0.0.0
CONFIG="$3" # one of "zest", "mu2", "mu2nosave"
ARGS="${@:4}"

# Make sure it is initialized
scp -i $PEMPATH github_key $REMOTE:.ssh/id_rsa
scp -i $PEMPATH known_hosts $REMOTE:.ssh/known_hosts
# scp -i $PEMPATH -r $HOME/Work/fuzz_chocopy/src/main/java/chocopy/reference/ $REMOTE:
ssh -i $PEMPATH $REMOTE "bash -s" < init.sh

# Run fuzzing sessions (with repetitions) using multiple screens
ssh -i $PEMPATH $REMOTE FUZZ_CONFIG=$CONFIG FUZZ_ARGS=\"$ARGS\" "bash -s" < fuzz_run.sh
