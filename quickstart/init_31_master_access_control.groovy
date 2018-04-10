import jenkins.model.*
import java.util.logging.Logger

import jenkins.security.s2m.*

def configName = "init_31_master_access_control.groovy"
Logger logger = Logger.getLogger("$configName")

def j = Jenkins.getInstance()

File disableScript = new File(j.rootDir, ".disable-$configName")
if (disableScript.exists()) {
    logger.info("DISABLED $configName")
    return
}

logger.info("begin aster_access_control config")

j.injector.getInstance(AdminWhitelistRule.class)
    .setMasterKillSwitch(false);
j.save()

logger.info("configured aster_access_control")

//create marker file to disable scripts from running twice
disableScript.createNewFile()