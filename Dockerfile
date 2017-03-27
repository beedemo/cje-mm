FROM cloudbees/cje-mm:2.32.2.6

#set java opts variable to skip setup wizard; plugins will be installed via license activated script
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
