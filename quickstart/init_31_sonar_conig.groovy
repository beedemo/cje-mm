import jenkins.model.Jenkins

import com.cloudbees.plugins.credentials.CredentialsProvider
import org.jenkinsci.plugins.plaincredentials.StringCredentials
import com.cloudbees.plugins.credentials.CredentialsMatchers

import hudson.plugins.sonar.SonarInstallation
import hudson.plugins.sonar.SonarGlobalConfiguration
import static hudson.plugins.sonar.utils.SQServerVersions.SQ_5_3_OR_HIGHER

import java.util.logging.Logger

String scriptName = "init_31_sonar_conig"

Logger logger = Logger.getLogger(scriptName)

File disableScript = new File(Jenkins.getInstance().getRootDir(), ".disable-$scriptName")
if (disableScript.exists()) {
    logger.info("DISABLE install plugins script")
    return
}

logger.info("Begin Configuring Sonarqube")

j = Jenkins.getInstance()

//need to get credentials from CJOC as the sonar plugin doesn't support credentials
StringCredentials cred = null
String credentialsId = "sonar.beedemo"

logger.info("checking for provided credentials based on id of $credentialsId")

List<StringCredentials> candidates = new ArrayList<StringCredentials>();
candidates.addAll(CredentialsProvider.lookupCredentials(org.jenkinsci.plugins.plaincredentials.StringCredentials.class, j.instance));

cred = CredentialsMatchers.firstOrNull(candidates, CredentialsMatchers.withId(credentialsId))

def sonarGlobalConfiguration = new SonarGlobalConfiguration()
sonarGlobalConfiguration.setInstallations(new SonarInstallation("beedemo", "https://sonar.k8s.beedemo.net", SQ_5_3_OR_HIGHER, cred.secret.plainText, null, null, null, null, null, null, null, null, null))

logger.info("Done Configuring Sonarqube")

//create marker file to disable scripts from running twice
disableScript.createNewFile()