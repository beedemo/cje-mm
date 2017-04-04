import jenkins.model.Jenkins

import java.util.logging.Logger

Logger logger = Logger.getLogger("init.d init_99_quickstart_save.groovy")
    
File disableSaveScript = new File(Jenkins.getInstance().getRootDir(), ".disable-init_99_quickstart_save-script")
if (disableSaveScript.exists()) {
    logger.info("DISABLED init.d init_99_save script")
    return
} 

new File(Jenkins.getInstance().getRootDir(), ".disable-init-script").createNewFile()
logger.info("init.d configuration finished")
Jenkins.getInstance().save()

new File(Jenkins.getInstance().getRootDir(), ".disable-init_99_quickstart_save-script").createNewFile()
