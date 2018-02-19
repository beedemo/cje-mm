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

logger.info("init_10_global_flow_durability - setting Pipeline Durability to PERFORMANCE_OPTIMIZED")
GlobalDefaultFlowDurabilityLevel.DescriptorImpl level = jenkins.getExtensionList(GlobalDefaultFlowDurabilityLevel.DescriptorImpl.class).get(0);
level.setDurabilityHint(FlowDurabilityHint.PERFORMANCE_OPTIMIZED);
logger.info("init_10_global_flow_durability - Pipeline Durability set to PERFORMANCE_OPTIMIZED")

//create marker file to disable scripts from running twice
disableScript.createNewFile()