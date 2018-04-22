#!groovy
def label = "kaniko-${UUID.randomUUID().toString()}"

 podTemplate(name: 'kaniko', label: label, yaml: """
 kind: Pod
 metadata:
   name: kaniko
 spec:
   containers:
   - name: kaniko
     image: csanchez/kaniko:jenkins # we need a patched version of kaniko for now
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
 """
   ) {

   node(label) {
     stage('Build with Kaniko') {
       checkout scm
       container('kaniko') {
           sh '/kaniko/executor -c . --destination=https://registry-1.docker.io/beedemo/cje-mm:kube-kaniko-1'
       }
     }
   }
 }
