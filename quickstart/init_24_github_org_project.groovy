import hudson.model.*;
import jenkins.model.*;

import hudson.security.ACL;
import jenkins.branch.OrganizationFolder;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.scm.api.SCMSourceOwners;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;

import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import com.cloudbees.plugins.credentials.CredentialsMatchers

import java.util.logging.Logger

Logger logger = Logger.getLogger("init_24_github_org_project.groovy")

def j = Jenkins.instance

File disableScript = new File(j.rootDir, ".disable-init_24_github_org_project")
if (disableScript.exists()) {
    logger.info("DISABLED init_24_github_org_project script")
    return
}

def masterName = System.properties.'MASTER_NAME'
if(masterName != null) {
    def cbWarProfile = System.properties.'cb.IMProp.warProfiles'
    logger.info("init_24_github_org_project cb.IMProp.warProfiles=$cbWarProfile")

    def credentialsId = masterName

    StandardUsernamePasswordCredentials cred = null

    logger.info("about to add basic auth for HttpRequest plugin global config")
    
    List<StandardUsernamePasswordCredentials> candidates = new ArrayList<StandardUsernamePasswordCredentials>();
    candidates.addAll(CredentialsProvider.lookupCredentials(com.cloudbees.plugins.credentials.common.StandardUsernameCredentials.class, Jenkins.instance));

    cred = CredentialsMatchers.firstOrNull(candidates, CredentialsMatchers.withId(credentialsId))
    if ( cred ) {
        def jobName = cred.description
        logger.info("using credential description $jobName for GitHub Org name in init_24_github_org_project script - creating GitHub Org Folder")
        
        println "--> creating $jobName"
        def jobConfigXml = """
        <jenkins.branch.OrganizationFolder plugin="branch-api@2.0.11">
          <actions>
            <io.jenkins.blueocean.service.embedded.BlueOceanUrlAction plugin="blueocean-rest-impl@1.3.3">
              <blueOceanUrlObject class="io.jenkins.blueocean.service.embedded.BlueOceanUrlObjectImpl">
                <mappedUrl>blue/organizations/jenkins/pipelines/</mappedUrl>
                <modelObject class="jenkins.branch.OrganizationFolder" reference="../../../.."/>
              </blueOceanUrlObject>
            </io.jenkins.blueocean.service.embedded.BlueOceanUrlAction>
          </actions>
          <description/>
          <properties>
            <com.cloudbees.hudson.plugins.folder.properties.EnvVarsFolderProperty plugin="cloudbees-folders-plus@3.3">
              <properties/>
            </com.cloudbees.hudson.plugins.folder.properties.EnvVarsFolderProperty>
            <org.jenkinsci.plugins.pipeline.modeldefinition.config.FolderConfig plugin="pipeline-model-definition@1.2.2">
              <dockerLabel/>
              <registry plugin="docker-commons@1.9"/>
            </org.jenkinsci.plugins.pipeline.modeldefinition.config.FolderConfig>
            <jenkins.branch.NoTriggerOrganizationFolderProperty>
              <branches>.*</branches>
            </jenkins.branch.NoTriggerOrganizationFolderProperty>
          </properties>
          <folderViews class="jenkins.branch.OrganizationFolderViewHolder">
            <owner reference="../.."/>
          </folderViews>
          <healthMetrics>
            <com.cloudbees.hudson.plugins.folder.health.WorstChildHealthMetric plugin="cloudbees-folder@6.1.2">
              <nonRecursive>false</nonRecursive>
            </com.cloudbees.hudson.plugins.folder.health.WorstChildHealthMetric>
            <com.cloudbees.hudson.plugins.folder.health.AverageChildHealthMetric plugin="cloudbees-folders-plus@3.3"/>
            <com.cloudbees.hudson.plugins.folder.health.JobStatusHealthMetric plugin="cloudbees-folders-plus@3.3">
              <success>true</success>
              <failure>true</failure>
              <unstable>true</unstable>
              <unbuilt>true</unbuilt>
              <countVirginJobs>false</countVirginJobs>
            </com.cloudbees.hudson.plugins.folder.health.JobStatusHealthMetric>
            <com.cloudbees.hudson.plugins.folder.health.ProjectEnabledHealthMetric plugin="cloudbees-folders-plus@3.3"/>
          </healthMetrics>
          <icon class="jenkins.branch.MetadataActionFolderIcon">
            <owner class="jenkins.branch.OrganizationFolder" reference="../.."/>
          </icon>
          <orphanedItemStrategy class="com.cloudbees.hudson.plugins.folder.computed.DefaultOrphanedItemStrategy" plugin="cloudbees-folder@6.1.2">
            <pruneDeadBranches>true</pruneDeadBranches>
            <daysToKeep>-1</daysToKeep>
            <numToKeep>-1</numToKeep>
          </orphanedItemStrategy>
          <triggers>
            <com.cloudbees.hudson.plugins.folder.computed.PeriodicFolderTrigger plugin="cloudbees-folder@6.1.2">
              <spec>H H * * *</spec>
              <interval>604800000</interval>
            </com.cloudbees.hudson.plugins.folder.computed.PeriodicFolderTrigger>
          </triggers>
          <disabled>false</disabled>
          <navigators>
            <org.jenkinsci.plugins.github__branch__source.GitHubSCMNavigator plugin="github-branch-source@2.2.6">
              <repoOwner>$jobName</repoOwner>
              <credentialsId>$credentialsId</credentialsId>
              <traits>
                <org.jenkinsci.plugins.github__branch__source.BranchDiscoveryTrait>
                  <strategyId>1</strategyId>
                </org.jenkinsci.plugins.github__branch__source.BranchDiscoveryTrait>
                <org.jenkinsci.plugins.github__branch__source.OriginPullRequestDiscoveryTrait>
                  <strategyId>1</strategyId>
                </org.jenkinsci.plugins.github__branch__source.OriginPullRequestDiscoveryTrait>
                <org.jenkinsci.plugins.github__branch__source.ForkPullRequestDiscoveryTrait>
                  <strategyId>1</strategyId>
                  <trust class="org.jenkinsci.plugins.github_branch_source.ForkPullRequestDiscoveryTrait\$TrustContributors"/>
                </org.jenkinsci.plugins.github__branch__source.ForkPullRequestDiscoveryTrait>
              </traits>
            </org.jenkinsci.plugins.github__branch__source.GitHubSCMNavigator>
          </navigators>
          <projectFactories>
            <org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProjectFactory plugin="workflow-multibranch@2.16">
              <scriptPath>Jenkinsfile</scriptPath>
            </org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProjectFactory>
            <com.cloudbees.workflow.multibranch.CustomMultiBranchProjectFactory plugin="cloudbees-workflow-template@2.7">
              <factory>
                <marker>pom.xml</marker>
                <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps@2.41">
                  <scm class="hudson.plugins.git.GitSCM" plugin="git@3.6.4">
                    <configVersion>2</configVersion>
                    <userRemoteConfigs>
                      <hudson.plugins.git.UserRemoteConfig>
                        <url>
        https://github.com/beedemo/custom-marker-pipelines.git
        </url>
                        <credentialsId>beedemo-user-github-token</credentialsId>
                      </hudson.plugins.git.UserRemoteConfig>
                    </userRemoteConfigs>
                    <branches>
                      <hudson.plugins.git.BranchSpec>
                        <name>*/master</name>
                      </hudson.plugins.git.BranchSpec>
                    </branches>
                    <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
                    <submoduleCfg class="list"/>
                    <extensions/>
                  </scm>
                  <scriptPath>pom-Jenkinsfile</scriptPath>
                  <lightweight>true</lightweight>
                </definition>
              </factory>
            </com.cloudbees.workflow.multibranch.CustomMultiBranchProjectFactory>
            <com.cloudbees.workflow.multibranch.CustomMultiBranchProjectFactory plugin="cloudbees-workflow-template@2.7">
              <factory>
                <marker>Dockerfile</marker>
                <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps@2.41">
                  <scm class="hudson.plugins.git.GitSCM" plugin="git@3.6.4">
                    <configVersion>2</configVersion>
                    <userRemoteConfigs>
                      <hudson.plugins.git.UserRemoteConfig>
                        <url>
        https://github.com/beedemo/custom-marker-pipelines.git
        </url>
                        <credentialsId>beedemo-user-github-token</credentialsId>
                      </hudson.plugins.git.UserRemoteConfig>
                    </userRemoteConfigs>
                    <branches>
                      <hudson.plugins.git.BranchSpec>
                        <name>*/master</name>
                      </hudson.plugins.git.BranchSpec>
                    </branches>
                    <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
                    <submoduleCfg class="list"/>
                    <extensions/>
                  </scm>
                  <scriptPath>Dockerfile-Jenkinsfile</scriptPath>
                  <lightweight>true</lightweight>
                </definition>
              </factory>
            </com.cloudbees.workflow.multibranch.CustomMultiBranchProjectFactory>
          </projectFactories>
        </jenkins.branch.OrganizationFolder>
        """
        //need to check for: cb.IMProp.warProfiles	bluesteel-core.json and move to bluesteel master folder for jobs to show up in Blue Ocean view
        if(cbWarProfile == 'bluesteel-core.json') {
            def folder = j.getItemByFullName(masterName)
            if (folder == null) {
              println "ERROR: Folder '$masterName' not found"
              return
            }
            job = folder.createProjectFromXML(jobName, new ByteArrayInputStream(jobConfigXml.getBytes("UTF-8")));
        } else {
            job = j.createProjectFromXML(jobName, new ByteArrayInputStream(jobConfigXml.getBytes("UTF-8")));
        }
        job.save()
        //the following will actually kickoff the initial scanning
        ACL.impersonate(ACL.SYSTEM, new Runnable() {
                        @Override public void run() {
                            for (final SCMSourceOwner owner : SCMSourceOwners.all()) {
                                if (owner instanceof OrganizationFolder) {
                                    OrganizationFolder orgFolder = (OrganizationFolder) owner;
                                    for (GitHubSCMNavigator navigator : orgFolder.getNavigators().getAll(GitHubSCMNavigator.class)) {
                                        orgFolder.scheduleBuild();
                                    }
                                }
                            }
                        }
                    });
        logger.info("created $jobName")
        //saving job again to create webhook - not sure why it isn't created on initial save
        job.save()
    } else {
        logger.info("System property MASTER_NAME does not match UsernamePassword credential id for init_24_github_org_project script - skipping GitHub Org Folder creation")
    }
} else {
    logger.info("System property MASTER_NAME NOT set or NOT available for init_24_github_org_project script - skipping GitHub Org Folder creation")
}
 //create marker file to disable scripts from running twice
 disableScript.createNewFile()