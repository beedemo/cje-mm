import hudson.security.HudsonPrivateSecurityRealm
import hudson.security.SecurityRealm
import jenkins.model.Jenkins
import java.util.logging.Logger

String scriptName = "init_03_authentication.groovy"
int version = 1
int markerVersion = 0

Logger logger = Logger.getLogger(scriptName)

Jenkins jenkins = Jenkins.getInstance()

File disableScript = new File(Jenkins.getInstance().getRootDir(), ".disable-authentication-script")
if (disableScript.exists()) {
    logger.info("DISABLE authentication script")
    return
}

File markerFile = new File(Jenkins.getInstance().getRootDir(), ".${scriptName}.done")
if (markerFile.exists()) {
    markerVersion = markerFile.text.toInteger()
}
if (markerVersion == version) {
    logger.info("$scriptName has already been executed for version $version, skipping execution");
    return
}

SecurityRealm securityRealm = jenkins.getSecurityRealm()

// SECURITY REALM
HudsonPrivateSecurityRealm hudsonPrivateSecurityRealm = new HudsonPrivateSecurityRealm(true, false, null)
hudsonPrivateSecurityRealm.createAccount("admin", "admin")

String securityRealmBefore = jenkins.getSecurityRealm().getClass().getName()

if (SecurityRealm.NO_AUTHENTICATION.equals(securityRealm)) {
    //jenkins has not yet been configured by the init script
    jenkins.setSecurityRealm(hudsonPrivateSecurityRealm)
    logger.fine("no security realm was set, enable " + jenkins.getSecurityRealm())
} else if (securityRealm instanceof HudsonPrivateSecurityRealm) {
    logger.fine("Security Realm has already been set to hudsonPrivateSecurityRealm, don't modify it")
} else {
    logger.fine("A custom security realm $securityRealm has been set, don't modify it")
}
String securityRealmAfter = jenkins.getSecurityRealm().getClass().getName()

logger.info("SECURITY REALM - BEFORE: " + securityRealmBefore + ", AFTER: " + securityRealmAfter)


if (markerFile.exists()) {
    markerFile.delete()
}
markerFile.withWriter { out ->
    out.println version
}