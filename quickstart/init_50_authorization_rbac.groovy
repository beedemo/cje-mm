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
     def jobName = "workshop"
     def jobConfigXml = """
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
             <properties class="hudson.model.View\$PropertyList"/>
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