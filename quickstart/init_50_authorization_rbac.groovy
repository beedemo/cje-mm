import jenkins.model.Jenkins
import hudson.model.Describable
import groovy.io.LineColumnReader
import groovy.json.JsonSlurper
import hudson.security.AuthorizationStrategy
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import nectar.plugins.rbac.groups.*;
import nectar.plugins.rbac.importers.AuthorizationStrategyImporter
import nectar.plugins.rbac.strategy.DefaultRoleMatrixAuthorizationConfig
import nectar.plugins.rbac.strategy.RoleMatrixAuthorizationConfig
import nectar.plugins.rbac.strategy.RoleMatrixAuthorizationPlugin
import nectar.plugins.rbac.strategy.RoleMatrixAuthorizationStrategyImpl
import java.lang.reflect.Field;
import hudson.scm.SCM;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.User;
import hudson.model.View;


import java.util.logging.Logger

String scriptName = "init_50_authorization_rbac.groovy"
int version = 1

int markerVersion = 0
Logger logger = Logger.getLogger(scriptName)

File disableScript = new File(Jenkins.getInstance().getRootDir(), ".disable-authorization-rbac-script")
if (disableScript.exists()) {
    logger.info("DISABLE authorization rbac script")
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

logger.info("Migrating from version $markerVersion to version $version")

Jenkins jenkins = Jenkins.getInstance()

AuthorizationStrategy authorizationStrategy = jenkins.getAuthorizationStrategy()

String authorizationStrategyBefore = authorizationStrategy.getClass().getName()

if (AuthorizationStrategy.UNSECURED.equals(authorizationStrategy)) {

    String ROLE_ADMINISTER = "administer";
    String ROLE_DEVELOP = "develop";
    String ROLE_BROWSE = "browse";
    String ROLE_ATTENDEE = "attendee";
    String ROLE_FOLDER_ADMIN = "folder-admin";
    PermissionGroup[] DEVELOP_PERMISSION_GROUPS = [Item.PERMISSIONS, SCM.PERMISSIONS, Run.PERMISSIONS, View.PERMISSIONS];

     RoleMatrixAuthorizationPlugin matrixAuthorizationPlugin = RoleMatrixAuthorizationPlugin.getInstance()
     RoleMatrixAuthorizationConfig config = new DefaultRoleMatrixAuthorizationConfig();
     RoleMatrixAuthorizationStrategyImpl roleMatrixAuthorizationStrategy = new RoleMatrixAuthorizationStrategyImpl()
     jenkins.setAuthorizationStrategy(roleMatrixAuthorizationStrategy)

     Map<String, Set<String>> roles = new HashMap<String, Set<String>>();
     for (Permission p : Permission.getAll()) {
         roles.put(p.getId(), new HashSet<String>(Collections.singleton(ROLE_ADMINISTER)));
     }
     roles.get(Jenkins.READ.getId()).add(ROLE_DEVELOP);
     for (PermissionGroup pg : DEVELOP_PERMISSION_GROUPS) {
         for (Permission p : pg.getPermissions()) {
             roles.get(p.getId()).add(ROLE_DEVELOP);
             roles.get(p.getId()).add(ROLE_FOLDER_ADMIN)
         }
     }
     roles.get(Jenkins.READ.getId()).add(ROLE_BROWSE);
     roles.get(Item.DISCOVER.getId()).add(ROLE_BROWSE);
     roles.get(Item.READ.getId()).add(ROLE_BROWSE);
     roles.get(Item.CREATE.getId()).add(ROLE_ATTENDEE);
     config.setRolesByPermissionIdMap(roles);
     config.setFilterableRoles(new HashSet<String>(Arrays.asList(ROLE_BROWSE, ROLE_DEVELOP)));
     List<Group> rootGroups = new ArrayList<Group>();
     Group g = new Group("Administrators");
     g.setMembers(Collections.singletonList("admin"));
     g.setRoleAssignments(Collections.singletonList(new Group.RoleAssignment(ROLE_ADMINISTER)));
     rootGroups.add(g);
     g = new Group("Browsers");
     g.setMembers(Collections.singletonList("authenticated"));
     g.setRoleAssignments(Collections.singletonList(new Group.RoleAssignment(ROLE_BROWSE)));
     rootGroups.add(g);
     config.setGroups(rootGroups);

     matrixAuthorizationPlugin.configuration = config
     matrixAuthorizationPlugin.save()
     logger.info("RBAC Roles and Groups defined")
     
     def jobName = "dev-folder"
     def jobConfigXml = """
     <com.cloudbees.hudson.plugins.modeling.impl.folder.FolderTemplate plugin="cloudbees-template@4.35">
       <actions>
         <io.jenkins.blueocean.service.embedded.BlueOceanUrlAction plugin="blueocean-rest-impl@1.4.2">
           <blueOceanUrlObject class="io.jenkins.blueocean.service.embedded.BlueOceanUrlObjectImpl">
             <mappedUrl>blue/organizations/jenkins/pipelines/</mappedUrl>
           </blueOceanUrlObject>
         </io.jenkins.blueocean.service.embedded.BlueOceanUrlAction>
       </actions>
       <description></description>
       <attributes>
         <template-attribute>
           <name>name</name>
           <displayName>Name</displayName>
           <control class="com.cloudbees.hudson.plugins.modeling.controls.TextFieldControl"/>
         </template-attribute>
       </attributes>
       <properties/>
       <instantiable>true</instantiable>
       <transformer class="com.cloudbees.hudson.plugins.modeling.transformer.GroovyTemplateModelTransformer">
         <template>&lt;com.cloudbees.hudson.plugins.folder.Folder plugin=&quot;cloudbees-folder@6.1.2&quot;&gt;
     &lt;actions&gt;
     &lt;io.jenkins.blueocean.service.embedded.BlueOceanUrlAction plugin=&quot;blueocean-rest-impl@1.3.0&quot;&gt;
     &lt;blueOceanUrlObject class=&quot;io.jenkins.blueocean.service.embedded.BlueOceanUrlObjectImpl&quot;&gt;
     &lt;mappedUrl&gt;blue/organizations/jenkins&lt;/mappedUrl&gt;
     &lt;/blueOceanUrlObject&gt;
     &lt;/io.jenkins.blueocean.service.embedded.BlueOceanUrlAction&gt;
     &lt;/actions&gt;
       &lt;actions/&gt;
       &lt;description/&gt;
       &lt;displayName&gt;\$name&lt;/displayName&gt;
     &lt;properties&gt;
     &lt;com.cloudbees.jenkins.plugins.foldersplus.SecurityGrantsFolderProperty plugin=&quot;cloudbees-folders-plus@3.2&quot;&gt;
     &lt;securityGrants/&gt;
     &lt;/com.cloudbees.jenkins.plugins.foldersplus.SecurityGrantsFolderProperty&gt;
     &lt;com.cloudbees.hudson.plugins.folder.properties.EnvVarsFolderProperty plugin=&quot;cloudbees-folders-plus@3.2&quot;&gt;
     &lt;properties/&gt;
     &lt;/com.cloudbees.hudson.plugins.folder.properties.EnvVarsFolderProperty&gt;
     &lt;org.jenkinsci.plugins.pipeline.modeldefinition.config.FolderConfig plugin=&quot;pipeline-model-definition@1.2.2&quot;&gt;
     &lt;dockerLabel/&gt;
     &lt;registry plugin=&quot;docker-commons@1.8&quot;/&gt;
     &lt;/org.jenkinsci.plugins.pipeline.modeldefinition.config.FolderConfig&gt;
     &lt;com.cloudbees.hudson.plugins.folder.properties.SubItemFilterProperty plugin=&quot;cloudbees-folders-plus@3.2&quot;&gt;
     &lt;allowedTypes&gt;
     &lt;string&gt;org.jenkinsci.plugins.workflow.job.WorkflowJob&lt;/string&gt;
     &lt;string&gt;org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject&lt;/string&gt;
     &lt;string&gt;jenkins.branch.OrganizationFolder.org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator&lt;/string&gt;
     &lt;/allowedTypes&gt;
     &lt;/com.cloudbees.hudson.plugins.folder.properties.SubItemFilterProperty&gt;
     &lt;% 
     if (instance.item != null &amp;&amp; instance.item.properties.get(com.cloudbees.hudson.plugins.folder.properties.FolderProxyGroupContainer) != null) { 
       println hudson.model.Items.XSTREAM.toXML(instance.item.properties.get(com.cloudbees.hudson.plugins.folder.properties.FolderProxyGroupContainer))
     } else {
     %&gt; 
         &lt;com.cloudbees.hudson.plugins.folder.properties.FolderProxyGroupContainer plugin=&quot;nectar-rbac@5.17&quot;&gt;
           &lt;groups&gt;
             &lt;nectar.plugins.rbac.groups.Group&gt;
               &lt;name&gt;\${name}-group&lt;/name&gt;
     &lt;% def user = hudson.model.User.current(); %&gt;
               &lt;member&gt;\${user.id}&lt;/member&gt;
               &lt;role&gt;folder-admin&lt;/role&gt;
             &lt;/nectar.plugins.rbac.groups.Group&gt;
           &lt;/groups&gt;
           &lt;roleFilters&gt;
             &lt;string&gt;folder-admin&lt;/string&gt;
             &lt;string&gt;develop&lt;/string&gt;
             &lt;string&gt;browse&lt;/string&gt;
           &lt;/roleFilters&gt;
         &lt;/com.cloudbees.hudson.plugins.folder.properties.FolderProxyGroupContainer&gt;
     &lt;% }   %&gt;
     &lt;/properties&gt;
     &lt;folderViews class=&quot;com.cloudbees.hudson.plugins.folder.views.DefaultFolderViewHolder&quot;&gt;
     &lt;views&gt;
     &lt;hudson.model.AllView&gt;
     &lt;owner class=&quot;com.cloudbees.hudson.plugins.folder.Folder&quot; reference=&quot;../../../..&quot;/&gt;
     &lt;name&gt;All&lt;/name&gt;
     &lt;filterExecutors&gt;false&lt;/filterExecutors&gt;
     &lt;filterQueue&gt;false&lt;/filterQueue&gt;
     &lt;/hudson.model.AllView&gt;
     &lt;/views&gt;
     &lt;tabBar class=&quot;hudson.views.DefaultViewsTabBar&quot;/&gt;
     &lt;/folderViews&gt;
     &lt;healthMetrics&gt;
     &lt;com.cloudbees.hudson.plugins.folder.health.WorstChildHealthMetric&gt;
     &lt;nonRecursive&gt;false&lt;/nonRecursive&gt;
     &lt;/com.cloudbees.hudson.plugins.folder.health.WorstChildHealthMetric&gt;
     &lt;com.cloudbees.hudson.plugins.folder.health.AverageChildHealthMetric plugin=&quot;cloudbees-folders-plus@3.2&quot;/&gt;
     &lt;com.cloudbees.hudson.plugins.folder.health.JobStatusHealthMetric plugin=&quot;cloudbees-folders-plus@3.2&quot;&gt;
     &lt;success&gt;true&lt;/success&gt;
     &lt;failure&gt;true&lt;/failure&gt;
     &lt;unstable&gt;true&lt;/unstable&gt;
     &lt;unbuilt&gt;true&lt;/unbuilt&gt;
     &lt;countVirginJobs&gt;false&lt;/countVirginJobs&gt;
     &lt;/com.cloudbees.hudson.plugins.folder.health.JobStatusHealthMetric&gt;
     &lt;com.cloudbees.hudson.plugins.folder.health.ProjectEnabledHealthMetric plugin=&quot;cloudbees-folders-plus@3.2&quot;/&gt;
     &lt;/healthMetrics&gt;
     &lt;icon class=&quot;com.cloudbees.hudson.plugins.folder.icons.StockFolderIcon&quot;/&gt;
     &lt;/com.cloudbees.hudson.plugins.folder.Folder&gt;</template>
         <sandbox>false</sandbox>
       </transformer>
       <initActivities/>
     </com.cloudbees.hudson.plugins.modeling.impl.folder.FolderTemplate>
     """
     template = jenkins.createProjectFromXML(jobName, new ByteArrayInputStream(jobConfigXml.getBytes("UTF-8")));
     template.save()
     logger.info("created $jobName")
     
     jobName = "workshop"
     jobConfigXml = """
     <com.cloudbees.hudson.plugins.folder.Folder plugin="cloudbees-folder@6.3">
       <actions/>
       <description></description>
       <properties>
         <com.cloudbees.jenkins.plugins.foldersplus.SecurityGrantsFolderProperty plugin="cloudbees-folders-plus@3.4">
           <securityGrants/>
         </com.cloudbees.jenkins.plugins.foldersplus.SecurityGrantsFolderProperty>
         <com.cloudbees.hudson.plugins.folder.properties.EnvVarsFolderProperty plugin="cloudbees-folders-plus@3.4">
           <properties></properties>
         </com.cloudbees.hudson.plugins.folder.properties.EnvVarsFolderProperty>
         <org.jenkinsci.plugins.pipeline.modeldefinition.config.FolderConfig plugin="pipeline-model-definition@1.2.7">
           <dockerLabel></dockerLabel>
           <registry plugin="docker-commons@1.11"/>
         </org.jenkinsci.plugins.pipeline.modeldefinition.config.FolderConfig>
         <com.cloudbees.hudson.plugins.folder.properties.SubItemFilterProperty plugin="cloudbees-folders-plus@3.4">
           <allowedTypes>
             <string>dev-folder</string>
           </allowedTypes>
         </com.cloudbees.hudson.plugins.folder.properties.SubItemFilterProperty>
         <com.cloudbees.hudson.plugins.folder.properties.FolderProxyGroupContainer plugin="nectar-rbac@5.19">
           <groups>
             <nectar.plugins.rbac.groups.Group>
               <name>Attendees</name>
               <member>authenticated</member>
               <role propagateToChildren="false">attendee</role>
             </nectar.plugins.rbac.groups.Group>
           </groups>
         </com.cloudbees.hudson.plugins.folder.properties.FolderProxyGroupContainer>
       </properties>
       <folderViews class="com.cloudbees.hudson.plugins.folder.views.DefaultFolderViewHolder">
         <views>
           <hudson.model.AllView>
             <owner class="com.cloudbees.hudson.plugins.folder.Folder" reference="../../../.."/>
             <name>All</name>
             <filterExecutors>false</filterExecutors>
             <filterQueue>false</filterQueue>
           </hudson.model.AllView>
         </views>
         <tabBar class="hudson.views.DefaultViewsTabBar"/>
       </folderViews>
       <healthMetrics>
         <com.cloudbees.hudson.plugins.folder.health.WorstChildHealthMetric>
           <nonRecursive>true</nonRecursive>
         </com.cloudbees.hudson.plugins.folder.health.WorstChildHealthMetric>
       </healthMetrics>
       <icon class="com.cloudbees.hudson.plugins.folder.icons.StockFolderIcon"/>
     </com.cloudbees.hudson.plugins.folder.Folder>
     """
     folder = jenkins.createProjectFromXML(jobName, new ByteArrayInputStream(jobConfigXml.getBytes("UTF-8")));
     folder.save()
     logger.info("created $jobName")
} else {
    logger.fine("A custom authorization strategy has been set, don't modify it: " + authorizationStrategy)
}

String authorizationStrategyAfter = jenkins.getAuthorizationStrategy().getClass().getName()
logger.info("AUTHORIZATION STRATEGY - BEFORE: " + authorizationStrategyBefore + ", AFTER: " + authorizationStrategyAfter)

if (markerFile.exists()) {
    markerFile.delete()
}
markerFile.withWriter { out ->
    out.println version
}