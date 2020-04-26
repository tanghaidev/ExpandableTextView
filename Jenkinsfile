pipeline{

     agent any
    //agent {
        // Run on a build agent where we have the Android SDK installed
        //label 'android'
    //}
    stages {
        stage('Build APK'){
             steps {

                sh './gradlew clean && rm -rf ./app/build/' //②
                sh './gradlew assembleDebug'  //③
                archiveArtifacts '**/*.apk'
             }
            // post {
            //                   success {
             //                        // Notify if the upload succeeded
             //                        mail to: '492295503@qq.com', subject: 'New build available!', body: 'Check it out!'
              //                 }
              //             }
        }

    }

}