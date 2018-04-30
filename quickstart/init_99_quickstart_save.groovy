import jenkins.model.Jenkins



import java.util.logging.Logger

Logger logger = Logger.getLogger("init_99_quickstart_save.groovy")
    
File disableSaveScript = new File(Jenkins.getInstance().getRootDir(), ".disable-init_99_quickstart_save-script")
if (disableSaveScript.exists()) {
    logger.info("init_99_quickstart_save script has been disabled")
    return
}

Jenkins.getInstance().save()
logger.info("saving config changes, configuration finished")

new File(Jenkins.getInstance().getRootDir(), ".disable-init_99_quickstart_save-script").createNewFile()