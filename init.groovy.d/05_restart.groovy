import jenkins.model.Jenkins

import java.util.logging.Logger

Logger logger = Logger.getLogger("init.d 05_restart.groovy")



File disableSaveScript = new File(Jenkins.getInstance().getRootDir(), ".disable-05_restart-script")
if (disableSaveScript.exists()) {
    logger.info("DISABLED init.d .disable-05_restart script")
    return
} 

Thread.start {
      sleep 300000
      logger.info("preparing to restart Jenkins")
      Jenkins.instance.restart()
}

new File(Jenkins.getInstance().getRootDir(), ".disable-05_restart-script").createNewFile()