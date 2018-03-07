# CloudBees Jenkins Enterprise 
## Custom Managed Master Docker Image for the Introduction to Declarative Pipeline Workshop
The `intro-pipeline` branch version here provides additional configuration supporting the *Introduction to Declarative Pipeline Workshop*.

This repository provides an example for creating a custom Docker image to use as a [Managed Master](https://go.cloudbees.com/docs/cloudbees-documentation/admin-cje/getting-started/#provision-masters) *Docker image template* to be provisioned by CloudBees Jenkions Operations Center running on the CloudBees Jenkins Enterprise cluster. 

The image is configured to skip Jenkins 2 Setup Wizard, install all of the CloudBees recommended plugins (minus a few) and some additional plugins typically used by CloudBees SAs in demos and workshops, and auto-configure Jenkins. This *config-as-code* results in a streamlined CJE Cluster Managed Master provisioning process.

### Dockerfile
- The `Dockerfile` starts with a `FROM` value of the CloudBees Managed Master Docker image: `cloudbees/cje-mm`. 
- The `RUN /usr/local/bin/install-plugins.sh $(cat plugins.txt)` command installs all the plugins.
- The `quickstart` scripts modifies the Master configuration as specified below

#### Plugins installed:
See the [`plugins.txt`](plugins.txt) file to see all the plugins that get installed - some *non-CJE standard plugins* highlights include:

- [Blue Ocean with the Blue Ocean Pipeline Editor](https://jenkins.io/doc/book/blueocean/)
- HipChat plugin
- [Pipeline Utilities plugin](https://jenkins.io/doc/pipeline/steps/pipeline-utility-steps/)
- One-Shot Executor plugin

Note, the `install-plugins.sh` script will download the specified plugins and their dependencies at build time and include them in the image; it also inspects the Jenkins WAR and skips any plugins already included by CloudBees (embedded in the WAR).

#### initilization scripts (Groovy init scripts that run on Jenkins post initialization)
##### Regular init scripts (on startup)
- `init_99_save.groovy`: Ensures any previous configuration changes are saved, sets flags not to re-run certain scripts; currently not much done here as we prefer to wait for the CJE license to be activated

##### License Activated scripts - utilizes the custom CloudBees `license-activated-or-renewed-after-expiration` hook that will be fired after your CJE license is activated
- `init_01_install_plugins.groovy`: Used to install plugins listed in [`cje_plugins.txt`](license-activated/cje_plugins.txt), but now that is handled by the `Dockerfile` and this script is only used to trigger another custom groovy hook

##### Quickstart scripts - a custom init groovy hook that fires after required plugins are installed and after a necessary restart
- `init_03_global_pipeline_library.groovy`: Configures a Pipeline Global Shared Library from this repository https://github.com/beedemo/workflowLibs - see the Jenkins Pipeline documentation for [Extending with Shared Libraries ](https://jenkins.io/doc/book/pipeline/shared-libraries/)
- `init_04_pipeline_model-def_config.groovy`: Configures the agent label to be used for Pipeline Declarative Docker syntax. This is not documented very well, but there is a global and per folder setting to tell Declarative Pipeline what Jenkins agent `label` to use when using the `` syntax - to ensure that the underlying Pipeline Model Definition will be able to spin up the Docker image to use *inside* the agent.
- `init_07_palace_one_shot_config.groovy`: CJE 1.x includes an awesome Share Agent Cloud provisioner called [Palace](https://go.cloudbees.com/docs/cloudbees-documentation/pse-admin-guide/index.html#building). One of the features of Palace is to use the [One Shot Executor Strategy](https://github.com/jenkinsci/one-shot-executor-plugin), but because that strategy does not support Pipeline resumption, it is disabled by default. This script enables it by default - because it makes agent provisioning super fast!
- [`init_10_global_flow_durability.groovy`](quickstart/init_10_global_flow_durability.groovy): Sets the global **Pipeline Speed/Durability Setting** to `PERFORMANCE_OPTIMIZED`, this may be overridden per Pipeline job or per-branch for a Pipeline Multibranch project. See [Scaling Pipelines](https://jenkins.io/doc/book/pipeline/scaling-pipeline/) for more details. We might as well use maximum performance since we are using the One Shot Executor Strategy for agents.
- `init_12_http_request_global_config.groovy`: Creates Basic Digest Authentication entry for HttpRequest plugin for use with the Pipeline External shared libraries - REQUIRES environment variable `ES_AUTH_CREDENTIALS_ID` to be set to Jenkins Credential ID for Elasticsearch
- `init_15_hipchat_config.groovy`: Configures the global settings for the HipChat Notification plugin so the Managed Master will be able to send notifications to the designated Beedemo HipChat channel.
- `init_20_agent_protocol_config.groovy`: Disables all but the JNLP-4 protocol.

##### Dynamic Creation of GitHub Org Folder 
- [`init_24_github_org_project.groovy`](quickstart/init_24_github_org_project.groovy): Creates a Pipeline GitHub Org Folder if certain conditions are met.
    - A username/password credential with an id matching the name of the master you are creating exists on CJOC (NOTE: if using Team Masters the GitHub Org foler will be created in the Blue Steel folder with the same name as the Team Master)
    - Set the description of the credential to be the name of the GitHub Org - NOTE: the username/password (actually username and user access token) credential set must have the correct permissions for the GitHub Org specified
