pipeline{
    agent any
    stages {
        stage('Build'){
             steps {
                 dir('/data/shanhy'){
                       sh '(source /etc/profile;source ~/.bash_profile;sh ./demo.sh)'
                     }
                sh './gradlew clean && rm -rf ./app/build/' //②
                sh './gradlew assembleRelease'  //③
             }
        }

    }

}