import jenkins.model.Jenkins

import hudson.security.ACL
import jenkins.util.groovy.GroovyHookScript

import java.util.logging.Logger

Logger logger = Logger.getLogger("init_99_quickstart_save.groovy")
    
File disableSaveScript = new File(Jenkins.getInstance().getRootDir(), ".disable-init_99_quickstart_save-script")
File restartedFlag = new File(Jenkins.getInstance().getRootDir(), ".restarted-flag")
if (disableSaveScript.exists() && restartedFlag.exists()) {
    logger.info("DISABLED init_99_save script")
    return
} else if(restartedFlag.exists()) {
  runQuickstartHook()
}
new File(Jenkins.getInstance().getRootDir(), ".disable-init-script").createNewFile()
logger.info("configuration finished")
Jenkins.getInstance().save()

def runQuickstartHook() {
  ACL.impersonate(ACL.SYSTEM, new Runnable() {
    @Override
    public void run() {
      new GroovyHookScript("quickstart").run();
    }
  });
  new File(Jenkins.getInstance().getRootDir(), ".disable-init_99_quickstart_save-script").createNewFile()
}