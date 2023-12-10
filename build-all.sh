#!/bin/bash
readonly GREEN='\033[0;32m'
readonly NOCOLOUR='\033[0m'
function echo_colour() {
	local -r colour="$1"
	local -r message="$2"

	echo -e "${colour}${message}${NOCOLOUR}"
}

bash gradlew clean runnableJar

if [ "$#" -lt 2 ]
then
  echo "Enter base file and save path!"
  exit 1
fi
readonly BASE_FILE=$1
readonly SAVE_DIR=$2
readonly ASSERTION_JAR=build/libs/AssertionExtractor-1.0-SNAPSHOT.jar
readonly JAVA=java

MODELS="atlas:toga:code2seq"
NUMBER_ASSERTIONS="1"
if [ "$#" -gt 2 ]
then
  NUMBER_ASSERTIONS=$3
fi

if [ "$#" -gt 3 ]
then
  MODELS=$4
fi

readarray -t ASSERTION_ARRAY < <(awk -F':' '{ for( i=1; i<=NF; i++ ) print $i }' <<<"${NUMBER_ASSERTIONS}")
readarray -t MODEL_ARRAY < <(awk -F':' '{ for( i=1; i<=NF; i++ ) print $i }' <<<"${MODELS}")

for model in "${MODEL_ARRAY[@]}"
do
  for assertion_number in "${ASSERTION_ARRAY[@]}"
  do
    echo_colour "$GREEN" "${model} with ${assertion_number} number of assertions"
    ${JAVA} -Xmx256g -jar ${ASSERTION_JAR}  --data-dir "${BASE_FILE}" --save-dir "${SAVE_DIR}/${assertion_number}" --model "${model}" -m "${assertion_number}"
  done
done