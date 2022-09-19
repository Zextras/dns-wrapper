@Library("zextras-library@0.6.0") _

def mvnCmd(String cmd) {
  sh 'mvn -B -Djooq.codegen.logging=WARN -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn ' + cmd
}

pipeline {
    agent {
        node {
            label 'openjdk13-agent-v1'
        }
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '50'))
        timeout(time: 2, unit: 'HOURS')
    }
    stages {
        stage('Test') {
            steps {
                mvnCmd('clean install')
                junit allowEmptyResults: true, testResults: 'target/surefire-reports/**/*.xml'
                stash includes: "target/**, ~/.m2/**", name: 'prj'
            }
        }
        stage('Deploy') {
            when {
                anyOf {
                    branch 'master'
                    buildingTag()
                }
            }
            steps {
                unstash 'prj'
                withCredentials([file(credentialsId: 'jenkins-maven-settings.xml', variable: 'SETTINGS_PATH')]) {
                    sh "cp ${SETTINGS_PATH} settings-jenkins.xml"
                }
                mvnCmd("--settings settings-jenkins.xml -DskipTests deploy")
            }
        }
    }
}
