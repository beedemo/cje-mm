#!groovy
def label = "kaniko-${UUID.randomUUID().toString()}"

 podTemplate(name: 'kaniko', label: label, yaml: """
 kind: Pod
 metadata:
   name: kaniko
 spec:
   containers:
   - name: kaniko
     image: csanchez/kaniko:jenkins
     imagePullPolicy: Always
     command:
     - cat
     tty: true
     imagePullSecrets:
       - name: jenkins-docker-cfg
 """
   ) {

   node(label) {
     stage('Build with Kaniko') {
       //checkout scm
       git 'https://github.com/jenkinsci/docker-jnlp-slave.git'
       container('kaniko') {
           sleep 100
           sh '/kaniko/executor -c . --destination=beedemo/jnlp-agent:kaniko-1'
       }
     }
   }
 }
