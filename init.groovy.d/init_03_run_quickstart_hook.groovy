import jenkins.model.Jenkins

import java.nio.file.Path
import java.nio.file.Paths

import hudson.security.ACL
import jenkins.util.groovy.GroovyHookScript

import java.util.logging.Logger

String scriptName = "init_03_run_quickstart_hook"

Logger logger = Logger.getLogger(scriptName)

def env = System.getenv()

//allows re-running quickstart scripts that may have required a restart
def runQuickstart = env['RUN_QUICKSTART_HOOK']
if(runQuickstart != null) {
    //kickoff quickstart scripts once licensed and plugins are installed
    ACL.impersonate(ACL.SYSTEM, new Runnable() {
        @Override
        public void run() {
          new GroovyHookScript("quickstart").run();
        }
    });
}