import jenkins.model.Jenkins

import java.util.logging.Logger

Logger logger = Logger.getLogger("init.d init_99_quickstart_save.groovy")
    
File disableSaveScript = new File(Jenkins.getInstance().getRootDir(), ".disable-init_99_quickstart_save-script")
if (disableSaveScript.exists()) {
    logger.info("DISABLED init.d init_99_save script")
    return
} 

logger.info("before thread for restart - restart in 90 seconds")
Thread.start {
      logger.info("sleeping for 90 seconds before restart")
      sleep 90000
      logger.info("preparing to restart Jenkins")
      Jenkins.instance.restart()
}
logger.info("init.d configuration finished")
Jenkins.getInstance().save()

new File(Jenkins.getInstance().getRootDir(), ".disable-init_99_quickstart_save-script").createNewFile()
