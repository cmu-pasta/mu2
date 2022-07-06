#!/bin/bash

set -e

if [ $# -lt 4 ]; then
	echo "Usage: $0 PEMPATH REMOTE_SERVER FUZZ_CONFIG [FUZZ_ARGS..]" > /dev/stderr
	exit 1
fi

PEMPATH="$1" # example: ~/aws_keys/key.pem
CONFIG="$2" # one of "zest", "mu2", "mu2nosave"
BENCHMARK="$3"
REMOTE_RESULTS_DIR="/home/ubuntu/sort-benchmarks/target/$BENCHMARK-fuzz-results/$CONFIG"
INSTANCES_FILE="$4"
PROCS="$5"
ARGS="${@:6}"


function runFuzzing() {

  INSTANCE_ID=$1
  EXPERIMENT=$2
	echo "Running on instance $INSTANCE_ID for starting expeirment $EXPERIMENT"
  aws ec2 start-instances --instance-ids $INSTANCE_ID
  aws ec2 wait instance-running --instance-ids $INSTANCE_ID
  IP=`aws ec2 describe-instances --instance-ids $INSTANCE_ID --query 'Reservations[*].Instances[*].PublicIpAddress' --output text`
  echo $IP
  REMOTE="ubuntu@$IP"
  # Make sure it is initialized
  scp -o StrictHostKeyChecking=accept-new -i $PEMPATH github_key $REMOTE:.ssh/id_rsa
  scp -i $PEMPATH known_hosts $REMOTE:.ssh/known_hosts
  scp -i $PEMPATH -r $HOME/Work/fuzz_chocopy/src/main/java/chocopy/reference/ $REMOTE:
  ssh -i $PEMPATH $REMOTE "bash -s" < init.sh

  # Run fuzzing sessions (with repetitions) using multiple screens
  ssh -i $PEMPATH $REMOTE FUZZ_CONFIG=$CONFIG FUZZ_PROCS=\"$PROCS\" FUZZ_ARGS=\"$ARGS\" FUZZ_REP=\"$EXPERIMENT\" "bash -s" < fuzz_run.sh
  scp -i $PEMPATH -r $REMOTE:$REMOTE_RESULTS_DIR/* $HOME/mu2_summer_remote_results/$CONFIG/
	aws ec2 stop-instances --instance-ids $INSTANCE_ID

}

i=1
while read p; do
  runFuzzing $p $i &
	i=$(($i+$PROCS))
done < $INSTANCES_FILE
wait

