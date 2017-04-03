FROM cloudbees/cje-mm:2.32.3.1

#skip setup wizard
ENV BEEDEMO_JAVA_OPTS -Djenkins.install.runSetupWizard=false

USER root
#override jenkins.sh to add BEEDEMO_JAVA_OPTS
COPY jenkins.sh /usr/local/bin/jenkins.sh
ARG user=jenkins
USER ${user} 

#install CloudBees suggested plugins
COPY ./init.groovy.d/* /usr/share/jenkins/ref/init.groovy.d/
COPY ./license-activated/* /usr/share/jenkins/ref/license-activated-or-renewed-after-expiration.groovy.d/
COPY ./quickstart/* /usr/share/jenkins/ref/quickstart.groovy.d/

#install additional plugins
ENV JENKINS_UC http://jenkins-updates.cloudbees.com
COPY plugins.txt plugins.txt
COPY jenkins-support /usr/local/bin/jenkins-support
COPY install-plugins.sh /usr/local/bin/install-plugins.sh

RUN /usr/local/bin/install-plugins.sh $(cat plugins.txt)
