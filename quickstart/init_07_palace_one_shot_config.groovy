import jenkins.model.*;

import com.cloudbees.tiger.plugins.palace.PalaceOneShotConfiguration;

import java.util.logging.Logger;


Logger logger = Logger.getLogger("quickstart.init_07_palace_one_shot_config.groovy")
logger.info("BEGIN init_07_palace_one_shot_config script")
File disableScript = new File(Jenkins.getInstance().getRootDir(), ".disable-init_07_palace_one_shot_config-script")
if (disableScript.exists()) {
    logger.info("DISABLE init_07_palace_one_shot_config script")
    return
}


PalaceOneShotConfiguration palaceOneShotConfig = GlobalConfiguration.all().get(PalaceOneShotConfiguration.class)
palaceOneShotConfig.setEnabled(true)

logger.info("FINISH init_07_palace_one_shot_config script")
disableScript.createNewFile()