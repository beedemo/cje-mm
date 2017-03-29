import jenkins.model.*;

import org.jenkinsci.plugins.pipeline.modeldefinition.config.GlobalConfig;
import jenkins.scm.api.SCMSource;

import java.util.logging.Logger;


Logger logger = Logger.getLogger("init.init_04_pipeline_mode-def_config.groovy")
logger.info("BEGIN docker label for pipeline-model-definition-config")
File disableScript = new File(Jenkins.getInstance().getRootDir(), ".disable-pipeline-model-definition-config-script")
if (disableScript.exists()) {
    logger.info("DISABLE pipeline-model-definition-config script")
    return
}

GlobalConfig globalConfig = GlobalConfiguration.all().get(GlobalConfig.class)
globalConfig.setDockerLabel("docker-cloud")
logger.info("setting docker label for pipeline-model-definition-config")

disableScript.createNewFile()