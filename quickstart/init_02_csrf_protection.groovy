import hudson.model.*;
import jenkins.model.*;
import hudson.security.csrf.DefaultCrumbIssuer

import java.util.logging.Logger

def scriptName = "init_02_csrf_protection"

Logger logger = Logger.getLogger("init_02_csrf_protection.groovy")

def j = Jenkins.instance

File disableScript = new File(j.rootDir, ".disable-$scriptName")
if (disableScript.exists()) {
    logger.info("DISABLED $scriptName")
    return
}

if(j.getCrumbIssuer() == null) {
    j.setCrumbIssuer(new DefaultCrumbIssuer(true))
    j.save()
    logger.info("CSRF Protection configuration has changed.  Enabled CSRF Protection.")
}
else {
    logger.info("Nothing changed.  CSRF Protection already configured.")
}
//create marker file to disable scripts from running twice
disableScript.createNewFile()
