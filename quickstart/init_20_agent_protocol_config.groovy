import jenkins.model.*
import java.util.logging.Logger

def configName = "init_20_agent_protocol_config.groovy"
Logger logger = Logger.getLogger("$configName")

def j = Jenkins.getInstance()

File disableScript = new File(j.rootDir, ".disable-$configName")
if (disableScript.exists()) {
    logger.info("DISABLED $configName")
    return
}

logger.info("begin agent_protocol_config")

Set<String> agentProtocolsList = ['JNLP4-connect', 'Ping']
if(!j.getAgentProtocols().equals(agentProtocolsList)) {
    j.setAgentProtocols(agentProtocolsList)
    println "Agent Protocols have changed.  Setting: ${agentProtocolsList}"
    j.save()
}
else {
    println "Nothing changed.  Agent Protocols already configured: ${j.getAgentProtocols()}"
}

logger.info("configured agent_protocol")

//create marker file to disable scripts from running twice
disableScript.createNewFile()