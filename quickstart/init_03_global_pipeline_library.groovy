import jenkins.model.*;

import org.jenkinsci.plugins.workflow.libs.*;
import jenkins.scm.api.SCMSource;

import java.util.logging.Logger;


Logger logger = Logger.getLogger("init.init_03_global_pipeline_library.groovy")
logger.info("BEGIN global_pipeline_library script")
File disableScript = new File(Jenkins.getInstance().getRootDir(), ".disable-global_libs-script")
if (disableScript.exists()) {
    logger.info("DISABLE install plugins script")
    return
}


GlobalLibraries globalLibs = GlobalConfiguration.all().get(GlobalLibraries.class)

SCMSource scm = new org.jenkinsci.plugins.github_branch_source.GitHubSCMSource(null, null, "SAME", "beedemo-user-github-token", "beedemo", "workflowLibs")
LibraryRetriever libRetriever = new SCMSourceRetriever(scm)
LibraryConfiguration libConfig = new LibraryConfiguration("BeedemoLibs", libRetriever)
libConfig.setDefaultVersion("master")
libConfig.setImplicit(true)

List<LibraryConfiguration> libraries= new ArrayList<LibraryConfiguration>()
libraries.add(libConfig)

globalLibs.setLibraries(libraries)
logger.info("FINISH global_pipeline_library script")
disableScript.createNewFile()