#! /bin/bash

set -ex

# Remove possible dead links (CLTS-1452)
if [ -L "${JENKINS_HOME}/init.groovy.d" ]; then
    echo "Broken symbolic link init.groovy.d found, deleting it..."
    rm ${JENKINS_HOME}/init.groovy.d
fi

if [ -L "${JENKINS_HOME}/license-activated-or-renewed-after-expiration.groovy.d" ] ; then
    echo "Broken symbolic link license-activated-or-renewed-after-expiration.groovy.d found, deleting it..."
    rm ${JENKINS_HOME}/license-activated-or-renewed-after-expiration.groovy.d
fi


# Copy files from /usr/share/jenkins/ref into $JENKINS_HOME
# So the initial JENKINS-HOME is set with expected content.
copy_reference_file() {
	f="${1%/}"
	b="${f%.override}"
	rel="${b:23}"
	version_marker="${rel}.version_from_image"
	dir=$(dirname "${b}")
	local action;
	local reason;
	local container_version;
	local image_version;
	local marker_version;
	local log; log=false
	if [[ ! -e $JENKINS_HOME/${rel} || $f = *.override ]]; then
        action="INSTALLED"
        log=true
        mkdir -p "$JENKINS_HOME/${dir:23}"
        cp -r "${f}" "$JENKINS_HOME/${rel}";
    else
        action="SKIPPED"
    fi
	if [[ -n "$VERBOSE" || "$log" == "true" ]]; then
        if [ -z "$reason" ]; then
            echo "$action $rel" | tee -a "$COPY_REFERENCE_FILE_LOG"
        else
            echo "$action $rel : $reason" | tee -a "$COPY_REFERENCE_FILE_LOG"
        fi
	fi
}
: ${JENKINS_HOME:="/var/jenkins_home"}
export -f copy_reference_file
touch "${COPY_REFERENCE_FILE_LOG}" || (echo "Can not write to ${COPY_REFERENCE_FILE_LOG}. Wrong volume permissions?" && exit 1)
echo "--- Copying files at $(date)" | tee -a "$COPY_REFERENCE_FILE_LOG"
find /usr/share/jenkins/ref/ -type f -exec bash -c "copy_reference_file '{}'" \;

# if `docker run` first argument start with `--` the user is passing jenkins launcher arguments
if [[ $# -lt 1 ]] || [[ "$1" == "--"* ]]; then
  eval "exec java  -server -XX:+AlwaysPreTouch XX:+UseConcMarkSweepGC $BEEDEMO_JAVA_OPTS $JAVA_OPTS -jar /usr/share/jenkins/jenkins.war $JENKINS_OPTS \"\$@\""
fi

# As argument is not jenkins, assume user want to run his own process, for sample a `bash` shell to explore this image
exec "$@"