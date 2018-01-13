import jenkins.model.*
import java.util.logging.Logger

def configName = "init_15_hipchat_config.groovy"
Logger logger = Logger.getLogger("$configName")

def j = Jenkins.getInstance()

File disableScript = new File(j.rootDir, ".disable-$configName")
if (disableScript.exists()) {
    logger.info("DISABLED $configName")
    return
}

logger.info("begin hipchat config")

def desc = j.getDescriptor("jenkins.plugins.hipcaht.HipChatNotifier")

    hipchat = j.getDescriptorByType(jenkins.plugins.hipcaht.HipChatNotifier.DescriptorImpl)
    hipchat.server = "cloudbees.hipchat.com"
    hipchat.v2Enabled = true
    hipchat.credentialId = "hipchat-sa-demo-environment"
    hipchat.room = "1613593"
    hipchat.sendAs = "Beedemo CJE"
    hipchat.save()

logger.info("configured hipchat")

//create marker file to disable scripts from running twice
disableScript.createNewFile()