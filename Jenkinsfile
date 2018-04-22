#!groovy
def label = "kaniko-${UUID.randomUUID().toString()}"

 podTemplate(name: 'kaniko', label: label, yaml: """
 kind: Pod
 metadata:
   name: kaniko
 spec:
   containers:
   - name: kaniko
     image: gcr.io/kaniko-project/executor:7ceba77ef0308652c7a2e884aaa86011d92906a7
     imagePullPolicy: Always
     command:
     - cat
     tty: true
     volumeMounts:
       - name: jenkins-docker-cfg
         mountPath: /root/.docker
   volumes:
     - name: jenkins-docker-cfg
       secret:
         secretName: jenkins-docker-cfg
         items:
         - key: .dockerconfigjson
           path: config.json
 """
   ) {

   node(label) {
     stage('Build with Kaniko') {
       checkout scm
       container('kaniko') {
           sh '/kaniko/executor -c . --destination=beedemo/cje-mm:kaniko-1'
       }
     }
   }
 }
