import jenkins.model.*;
import jenkins.plugins.http_request.HttpRequestGlobalConfig;
import jenkins.plugins.http_request.auth.BasicDigestAuthentication;
import java.util.*;

import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import com.cloudbees.plugins.credentials.CredentialsMatchers

import java.util.logging.Logger

Logger logger = Logger.getLogger("init_12_http_request_global_config.groovy")

Jenkins j = Jenkins.getInstance()

File disableScript = new File(j.rootDir, ".disable-init_12_http_request_global_config")
if (disableScript.exists()) {
    logger.info("DISABLED init_12_http_request_global_config script")
    return
}

def env = System.getenv()
if(env['ES_AUTH_CREDENTIALS_ID'] != null) {

    def esAuthCredentialsId = env['ES_AUTH_CREDENTIALS_ID']

    StandardUsernamePasswordCredentials cred = null

    logger.info("about to add basic auth for HttpRequest plugin global config")
    
    List<StandardUsernamePasswordCredentials> candidates = new ArrayList<StandardUsernamePasswordCredentials>();
    candidates.addAll(CredentialsProvider.lookupCredentials(com.cloudbees.plugins.credentials.common.StandardUsernameCredentials.class, Jenkins.instance));

    cred = CredentialsMatchers.firstOrNull(candidates, CredentialsMatchers.withId(esAuthCredentialsId))

    if ( cred ) {
        println "found credential for id ${esAuthCredentialsId}"
        HttpRequestGlobalConfig httpReqConfig = GlobalConfiguration.all().get(HttpRequestGlobalConfig.class)

        BasicDigestAuthentication httpReqBasicAuth = new BasicDigestAuthentication("es-auth", cred.username, cred.password.toString())
        List<BasicDigestAuthentication> basicAuths = new ArrayList<BasicDigestAuthentication>()
        basicAuths.add(httpReqBasicAuth)
        httpReqConfig.setBasicDigestAuthentications(basicAuths)
        logger.info("added es-auth Basic Digest Authentication for HttpRequestGlobalConfig")

    } else {
        println "could not find credential for id ${esAuthCredentialsId}, skipping init_12_http_request_global_config"
    }
}
//create marker file to disable script from running twice
disableScript.createNewFile()