import jenkins.model.Jenkins



import java.util.logging.Logger

Logger logger = Logger.getLogger("init_99_quickstart_save.groovy")
    
File disableSaveScript = new File(Jenkins.getInstance().getRootDir(), ".disable-init_99_quickstart_save-script")
if (disableSaveScript.exists()) {
    logger.info("DISABLED init_99_quickstart_save script")
    return
}
Jenkins.getInstance().save()
logger.info("saving config changes, configuration finished")

Thread.start {
      logger.info("sleeping for 90 seconds before restart")
      sleep 90000
      logger.info("preparing to restart Jenkins")
      Jenkins.instance.restart()
}

new File(Jenkins.getInstance().getRootDir(), ".disable-init_99_quickstart_save-script").createNewFile()