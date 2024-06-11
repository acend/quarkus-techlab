#!/usr/bin/env bash

set -euo pipefail

version=unset
project=unset
diffonly=unset

usage() {
>&2 cat << EOF
Usage: $0
   [ -p | --project input (e.g. quarkus-rest-data-consumer) ]
   [ -v | --version input (e.g. 3.11.0) ]
   [ -d | --diff ]
   [ -h | --help ] 
EOF
exit 1
}
 
# Printing Git DIFF
run_diff() {
   for PROJ in ${PROJECTS[@]}; do
	echo -e "\n\n----------------------------------------------------------"
	echo -e "Project Git Diff: $PROJ"
	echo -e "----------------------------------------------------------"
        cat ${ROOT_PWD}/${DIFF_DIR}/${PROJ}_git.log
	echo -e "---\n"
   done
}

args=$(getopt -a -o p:v:dh --long project:,version:,diff,help -- "$@")
if [[ $? -gt 0 ]]; then
  usage
fi

eval set -- ${args}
while :
do
  case $1 in
    -p | --project)   project=$2   ; shift 2 ;;
    -v | --version)   version=$2   ; shift 2 ;;
    -d | --diff)    diffonly=yes   ; shift   ;;
    -h | --help)    usage      ; shift   ;;
    # -- means the end of the arguments; drop this, and break out of the while loop
    --) shift; break ;;
    *) >&2 echo Unsupported option: $1
       usage ;;
  esac
done

PROJECTS=(
	dev-services
	quarkus-getting-started
	quarkus-rest-data-consumer
	quarkus-rest-data-producer
	quarkus-reactive-rest-consumer
	quarkus-reactive-rest-producer
	quarkus-metrics-data-producer
	quarkus-metrics-data-consumer
	quarkus-opentelemetry-jaeger
	quarkus-reactive-messaging-consumer
	quarkus-reactive-messaging-producer
	quarkus-cloudevents-consumer
	quarkus-cloudevents-producer
	techlab-extension-appinfo
	quarkus-appinfo-application
)
SOLUTION_DIR="solution"
DIFF_DIR_PREFIX="diff-update"
ROOT_PWD=`pwd`


if [ "${project}" == "unset" ]; then 
	echo "Using default project list"; 
else 
	echo "Using project $project";
	if [ ! -d ${SOLUTION_DIR}/${project} ]; then
	    echo "Project $project specified but does not exist."
	    exit 1
	fi
	PROJECTS=("$project")
fi

if [ "${version}" == "unset" ]; then 
	echo "Quarkus version not specified"; 
	version=`quarkus --version`
fi

DIFF_DIR=${DIFF_DIR_PREFIX}/${version}
TS=`date +%Y%m%d%H%M%S`
echo "Upgrade Quarkus version: $version"
echo "Latest Quarkus version:  `quarkus --version`"
echo "Diff output directory: ${DIFF_DIR}"
echo "Run Timestamp: $TS"

if [ "${diffonly}" == "yes" ]; then 
	echo "Running diff only"; 
	run_diff
	exit
fi

if [ ! -d $DIFF_DIR ]; then
	echo "Creating ${DIFF_DIR}"
	mkdir -p $DIFF_DIR
fi

for PROJ in ${PROJECTS[@]}; do
	cd ${ROOT_PWD}/${SOLUTION_DIR}/${PROJ}
	echo "Updating Project ${SOLUTION_DIR}/${PROJ}"
	quarkus update --platform-version=$version

	echo "Writing git diff to: ${ROOT_PWD}/${DIFF_DIR}/${PROJ}.log"
	git diff . > ${ROOT_PWD}/${DIFF_DIR}/${PROJ}_git.log 2>&1
	if [[ "${PROJ}" == *"extension"* ]]; then
		echo "$PROJ is an quarkus extension. Running maven package install."
		mvn clean package install > ${ROOT_PWD}/${DIFF_DIR}/${PROJ}_maven.log 2>&1
	else
		echo "$PROJ is a quarkus application. Running maven test"
		./mvnw clean test > ${ROOT_PWD}/${DIFF_DIR}/${PROJ}_maven.log 2>&1
	fi

	if [[ "$?" -ne 0 ]]; then
		echo 'Running tests failed.'; 
		exit $rc
	fi

	echo "Generating dependencies csv for ${SOLUTION_DIR}/${PROJ}/pom.xml"
	xsltproc ${ROOT_PWD}/dependencies.xsl pom.xml > dependencies.csv

	cd $ROOT_PWD
done

run_diff


echo "all done."


