import hudson.model.*;
import jenkins.model.*;

import hudson.security.ACL;
import jenkins.branch.OrganizationFolder;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.scm.api.SCMSourceOwners;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;

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

    def credentialsId = masterName

    StandardUsernamePasswordCredentials cred = null

    logger.info("about to add basic auth for HttpRequest plugin global config")
    
    List<StandardUsernamePasswordCredentials> candidates = new ArrayList<StandardUsernamePasswordCredentials>();
    candidates.addAll(CredentialsProvider.lookupCredentials(com.cloudbees.plugins.credentials.common.StandardUsernameCredentials.class, Jenkins.instance));

    cred = CredentialsMatchers.firstOrNull(candidates, CredentialsMatchers.withId(credentialsId))
    if ( cred ) {

        logger.info("env variable GITHUB_ORG set for init_24_github_org_project script - creating GitHub Org Folder for " + env['GITHUB_ORG'])
        def jobName = cred.description
        def scanCredentialsId = masterName

        println "--> creating $jobName"
        def jobConfigXml = """
        <jenkins.branch.OrganizationFolder plugin="branch-api@1.11">
          <displayName>$jobName</displayName>
          <properties>
            <com.cloudbees.hudson.plugins.folder.properties.EnvVarsFolderProperty plugin="cloudbees-folders-plus@3.0">
              <properties/>
            </com.cloudbees.hudson.plugins.folder.properties.EnvVarsFolderProperty>
            <org.jenkinsci.plugins.workflow.libs.FolderLibraries plugin="workflow-cps-global-lib@2.4">
            <libraries>
            <org.jenkinsci.plugins.workflow.libs.LibraryConfiguration>
            <name>BeedemoLibs</name>
            <retriever class="org.jenkinsci.plugins.workflow.libs.SCMRetriever">
            <scm class="hudson.plugins.git.GitSCM" plugin="git@2.5.3">
            <configVersion>2</configVersion>
            <userRemoteConfigs>
            <hudson.plugins.git.UserRemoteConfig>
            <url>https://github.com/beedemo/workflowLibs.git</url>
            <credentialsId>beedemo-user-github-token</credentialsId>
            </hudson.plugins.git.UserRemoteConfig>
            </userRemoteConfigs>
            <branches>
            <hudson.plugins.git.BranchSpec>
            <name>\${library.BeedemoLibs.version}</name>
            </hudson.plugins.git.BranchSpec>
            </branches>
            <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
            <submoduleCfg class="list"/>
            <extensions/>
            </scm>
            </retriever>
            <defaultVersion>master</defaultVersion>
            <implicit>true</implicit>
            <allowVersionOverride>true</allowVersionOverride>
            </org.jenkinsci.plugins.workflow.libs.LibraryConfiguration>
            </libraries>
            </org.jenkinsci.plugins.workflow.libs.FolderLibraries>
            <jenkins.branch.NoTriggerOrganizationFolderProperty>
              <branches>.*</branches>
            </jenkins.branch.NoTriggerOrganizationFolderProperty>
          </properties>
          <views>
            <hudson.model.ListView>
              <owner class="jenkins.branch.OrganizationFolder" reference="../../.."/>
              <name>Repositories</name>
              <filterExecutors>false</filterExecutors>
              <filterQueue>false</filterQueue>
              <properties class="hudson.model.View\$PropertyList"/>
              <jobNames>
                <comparator class="hudson.util.CaseInsensitiveComparator"/>
              </jobNames>
              <jobFilters/>
              <columns>
                <hudson.views.StatusColumn/>
                <hudson.views.WeatherColumn/>
                <org.jenkinsci.plugins.orgfolder.github.CustomNameJobColumn plugin="github-organization-folder@1.5">
                  <bundle>org.jenkinsci.plugins.orgfolder.github.Messages</bundle>
                  <key>ListViewColumn.Repository</key>
                </org.jenkinsci.plugins.orgfolder.github.CustomNameJobColumn>
                <org.jenkinsci.plugins.orgfolder.github.RepositoryDescriptionColumn plugin="github-organization-folder@1.5"/>
              </columns>
              <includeRegex>.*</includeRegex>
              <recurse>false</recurse>
            </hudson.model.ListView>
          </views>
          <viewsTabBar class="hudson.views.DefaultViewsTabBar"/>
          <primaryView>Repositories</primaryView>
          <healthMetrics>
            <com.cloudbees.hudson.plugins.folder.health.WorstChildHealthMetric plugin="cloudbees-folder@5.13"/>
            <com.cloudbees.hudson.plugins.folder.health.AverageChildHealthMetric plugin="cloudbees-folders-plus@3.0"/>
            <com.cloudbees.hudson.plugins.folder.health.JobStatusHealthMetric plugin="cloudbees-folders-plus@3.0">
              <success>true</success>
              <failure>true</failure>
              <unstable>true</unstable>
              <unbuilt>true</unbuilt>
              <countVirginJobs>false</countVirginJobs>
            </com.cloudbees.hudson.plugins.folder.health.JobStatusHealthMetric>
            <com.cloudbees.hudson.plugins.folder.health.ProjectEnabledHealthMetric plugin="cloudbees-folders-plus@3.0"/>
          </healthMetrics>
          <icon class="org.jenkinsci.plugins.orgfolder.github.GitHubOrgIcon" plugin="github-organization-folder@1.5">
            <folder class="jenkins.branch.OrganizationFolder" reference="../.."/>
          </icon>
          <orphanedItemStrategy class="com.cloudbees.hudson.plugins.folder.computed.DefaultOrphanedItemStrategy" plugin="cloudbees-folder@5.13">
            <pruneDeadBranches>true</pruneDeadBranches>
            <daysToKeep>0</daysToKeep>
            <numToKeep>0</numToKeep>
          </orphanedItemStrategy>
          <triggers>
            <com.cloudbees.hudson.plugins.folder.computed.PeriodicFolderTrigger plugin="cloudbees-folder@5.13">
              <spec>H H * * *</spec>
              <interval>86400000</interval>
            </com.cloudbees.hudson.plugins.folder.computed.PeriodicFolderTrigger>
          </triggers>
          <navigators>
            <org.jenkinsci.plugins.github__branch__source.GitHubSCMNavigator plugin="github-branch-source@1.10">
              <repoOwner>$jobName</repoOwner>
              <scanCredentialsId>$scanCredentialsId</scanCredentialsId>
              <checkoutCredentialsId>SAME</checkoutCredentialsId>
              <pattern>.*</pattern>
              <buildOriginBranch>true</buildOriginBranch>
              <buildOriginBranchWithPR>true</buildOriginBranchWithPR>
              <buildOriginPRMerge>false</buildOriginPRMerge>
              <buildOriginPRHead>false</buildOriginPRHead>
              <buildForkPRMerge>true</buildForkPRMerge>
              <buildForkPRHead>false</buildForkPRHead>
            </org.jenkinsci.plugins.github__branch__source.GitHubSCMNavigator>
          </navigators>
          <projectFactories>
            <org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProjectFactory plugin="workflow-multibranch@2.9"/>
            <com.cloudbees.workflow.multibranch.CustomMultiBranchProjectFactory plugin="cloudbees-workflow-template@2.4">
              <factory>
                <marker>pom.xml</marker>
                <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps@2.17">
                  <scm class="hudson.plugins.git.GitSCM" plugin="git@2.5.3">
                    <configVersion>2</configVersion>
                    <userRemoteConfigs>
                      <hudson.plugins.git.UserRemoteConfig>
                        <url>https://github.com/beedemo/custom-marker-pipelines.git</url>
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
                </definition>
              </factory>
            </com.cloudbees.workflow.multibranch.CustomMultiBranchProjectFactory>
            <com.cloudbees.workflow.multibranch.CustomMultiBranchProjectFactory plugin="cloudbees-workflow-template@2.4">
              <factory>
                <marker>Dockerfile</marker>
                <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps@2.17">
                  <scm class="hudson.plugins.git.GitSCM" plugin="git@2.5.3">
                    <configVersion>2</configVersion>
                    <userRemoteConfigs>
                      <hudson.plugins.git.UserRemoteConfig>
                        <url>https://github.com/beedemo/custom-marker-pipelines.git</url>
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
                </definition>
              </factory>
            </com.cloudbees.workflow.multibranch.CustomMultiBranchProjectFactory>
          </projectFactories>
        </jenkins.branch.OrganizationFolder>
        """

        job = j.createProjectFromXML(jobName, new ByteArrayInputStream(jobConfigXml.getBytes("UTF-8")));
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
    } else {
        logger.info("System property MASTER_NAME does not match UsernamePassword credential id for init_24_github_org_project script - skipping GitHub Org Folder creation")
    }
} else {
    logger.info("System property MASTER_NAME NOT set or NOT available for init_24_github_org_project script - skipping GitHub Org Folder creation")
}
 //create marker file to disable scripts from running twice
 disableScript.createNewFile()