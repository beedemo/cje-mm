FROM cloudbees/cje-mm:2.32.2.6

RUN /usr/local/bin/install-plugins.sh \
  hipchat
