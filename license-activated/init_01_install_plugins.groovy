import jenkins.model.Jenkins

import java.nio.file.Path
import java.nio.file.Paths

import java.util.logging.Logger

String scriptName = "init_01_install_plugins.groovy"

Logger logger = Logger.getLogger(scriptName)

File disableScript = new File(Jenkins.getInstance().getRootDir(), ".disable-install_plugins-script")
if (disableScript.exists()) {
    logger.info("DISABLE install plugins script")
    return
}

Jenkins jenkins = Jenkins.getInstance()

Path filePath = Paths.get('/var/jenkins_home/license-activated-or-renewed-after-expiration.groovy.d/cje_plugins.txt')
def plugins = filePath.toFile() as String[]
def pm = Jenkins.instance.pluginManager
plugins.each { pluginName ->
  logger.info("installing $pluginName")
  try {
      jenkins.instance.updateCenter.getPlugin(pluginName).deploy()
  } catch(Exception ex) {
      logger.info("unable to install plugin $pluginName")
      logger.info(ex.getMessage())
  }
}

disableScript.createNewFile()
//kickoff quickstart scripts not that plugins are installed
def runQuickstartHook() {
  ACL.impersonate(ACL.SYSTEM, new Runnable() {
    @Override
    public void run() {
      new GroovyHookScript("quickstart").run();
    }
  });
}