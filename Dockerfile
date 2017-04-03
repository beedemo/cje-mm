FROM cloudbees/cje-mm:2.32.3.1

#skip setup wizard
#ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
ENV JAVA_ARGS="-Djenkins.install.runSetupWizard=false"
#JAVA_OPTS don't appear to propagate correctly, so also using JAVA_ARGS above
ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false

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
