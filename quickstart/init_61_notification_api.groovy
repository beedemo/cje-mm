import jenkins.model.Jenkins
import hudson.ExtensionList

import com.cloudbees.jenkins.plugins.notification.api.NotificationConfiguration
import com.cloudbees.jenkins.plugins.notification.spi.Router
import com.cloudbees.opscenter.plugins.notification.OperationsCenterRouter

import java.util.logging.Logger

String scriptName = "init_61_notification_api.groovy"

Logger logger = Logger.getLogger(scriptName)
logger.info("running $scriptName")

jenkins = Jenkins.getInstance()

File disableScript = new File(jenkins.getRootDir(), ".disable-notification-api-script")
if (disableScript.exists()) {
    logger.info("DISABLE notification api script")
    return
}

NotificationConfiguration config = ExtensionList.lookupSingleton(NotificationConfiguration.class);
Router r = new OperationsCenterRouter();
        config.setRouter(r);
        config.setEnabled(true);
        config.onLoaded();

//create marker file to disable scripts from running twice
disableScript.createNewFile()