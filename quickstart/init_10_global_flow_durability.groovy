import hudson.model.*;
import jenkins.model.*;

import org.jenkinsci.plugins.workflow.flow.GlobalDefaultFlowDurabilityLevel
import org.jenkinsci.plugins.workflow.flow.FlowDurabilityHint

import java.util.logging.Logger

Logger logger = Logger.getLogger("init_10_global_flow_durability.groovy")

def jenkins = Jenkins.instance

File disableScript = new File(jenkins.rootDir, ".disable-init_10_global_flow_durability")
if (disableScript.exists()) {
    logger.info("DISABLED init_10_global_flow_durability script")
    return
}

logger.info("init_10_global_flow_durability - setting Pipeline Durability to SURVIVABLE_NONATOMIC")
GlobalDefaultFlowDurabilityLevel.DescriptorImpl level = jenkins.getExtensionList(GlobalDefaultFlowDurabilityLevel.DescriptorImpl.class).get(0);
level.setDurabilityHint(FlowDurabilityHint.SURVIVABLE_NONATOMIC);
logger.info("init_10_global_flow_durability - Pipeline Durability set to SURVIVABLE_NONATOMIC")

//create marker file to disable scripts from running twice
disableScript.createNewFile()