# CloudBees Jenkins Enterprise 
## Custom Managed Master Docker Image
This repository provides an example of how you may create custom Docker images to use as a [Managed Master](https://go.cloudbees.com/docs/cloudbees-documentation/admin-cje/getting-started/#provision-masters) template of sorts to be provisioned by CloudBees Jenkions Operations Center running on the CloudBees Jenkins Enterprise. 

The image is configured to skip Jenkins 2 Setup Wizard; and a specific set of CloudBees recommended plugins [`cje_plugins.txt`](license-activated/cje_plugins.txt) and some additional plugins will be installed; thus streamlining the provisioning process.

### Dockerfile
- The `Dockerfile` starts with a `FROM` value of the CloudBees Managed Master Docker image: `cloudbees/cje-mm`. 
- The `RUN /usr/local/bin/install-plugins.sh` command installs addition non-bundled plugins.
- The [`init_01_install_plugins.groovy`](license-activated/init_01_install_plugins.groovy) license-activated script automates the installation of specified plugins in the [`cje_plugins.txt`](license-activated/cje_plugins.txt) file.
- The `quickstart` scripts set configuration for the audit trail plugin, and optionally sets up basic auth configuration for the HTTP Request plugin and creates a GitHub Organization Folder job.

Besides the `Dockerfile`, the template consists of two primary customization components.

#### additional/upgraded plugins installed:
- http_request
- pipeline-utility-steps:1.3.0
- docker-commons:1.4.0
- dockerhub-notification
- hipchat

#### initilization scripts (Groovy init scripts that run on Jenkins startup)
##### Regular init scripts (on startup)
- `init_99_save.groovy`: Ensures any previous configuration changes are saved, sets flags not re-run certain scripts, and on restart initializes the custom `quickstart` hook

##### License Activated scripts
- `init_01_install_plugins.groovy`: Installs specific set of plugins listed in [`cje_plugins.txt`](license-activated/cje_plugins.txt), allowing the the Jenkin 2.x Setup Wizard to be skipped

##### Quickstart scripts - a custom init groovy hook that fires after required plugins are installed and after a necessary restart
- `init_03_global_pipeline_library.groovy`: Configures a Pipeline Global Shared Library from this repository https://github.com/beedemo/workflowLibs
- `init_04_pipeline_model-def_config.groovy`: Configures the agent label to be used for Pipeline Declarative Docker syntax
- `init_12_http_request_global_config.groovy`: Creates Basic Digest Authentication entry for HttpRequest plugin for use with the Pipeline External shared libraries - REQUIRES environment variable `ES_AUTH_CREDENTIALS_ID` to be set to Jenkins Credential ID for Elasticsearch
