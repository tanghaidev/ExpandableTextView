pipeline{
    agent {
        none
    }
    stages {
        stage('Build'){
             steps {
                sh './gradlew clean && rm -rf ./app/build/' //②
                sh './gradlew assembleRelease'  //③
             }
        }

    }

}