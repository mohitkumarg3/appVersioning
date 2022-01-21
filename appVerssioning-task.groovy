pipeline {
    
   agent any
    
 stages {
    
    stage('Git Checkout') {
        steps {
            git 'https://github.com/nxdevops/react-native-calendars'
        }
    }

    stage('Updating Latest Dependency') {
        steps {
            sh 'sudo rm -rf node_modules'
            sh 'sudo npm install'
            sh 'sudo npm install -g react-native-cli'
            sh 'sudo npm install -g firebase-tools'
        }
    }
    
    stage('Packaging App Bundle') {
        steps {
            script {
                def exists = fileExists 'android/app/src/main/assets/index.android.bundle'
                if (exists) {
                    echo 'File Already Exist!!!'
                    
                } else {
                    sh 'mkdir android/app/src/main/assets'
                    sh 'sudo chmod -R 0777 /tmp/'
                    sh 'sudo react-native bundle --platform android --dev false --entry-file index.js --bundle-output android/app/src/main/assets/index.android.bundle --assets-dest android/app/src/main/res/'
                }
            }
       }   
    }

    stage('Git Bump & Push Updated Version') {
        steps {
            sh 'sudo chmod -R 0777 /usr/local/bin/gitversion'
            sh 'gitversion bump patch'
            sh 'git push https://${gitToken}@github.com/nxdevops/react-native-calendars.git --tags'
        }
    }
    
    stage('Build & Release APK') {
        steps {
            dir("android") {
                sh 'sudo chmod -R 0777 /tmp/'
                sh 'sudo rm -rf app/src/main/res/drawable-* && sudo rm -rf app/src/main/res/raw/*'
                sh './gradlew assembleRelease'   
            }
        }
    }
    
    stage('APK Distribution') {
        steps {
            sh 'firebase appdistribution:distribute android/app/build/outputs/apk/release/app-release.apk --app "$APP_ID" --token "$FIREBASE_TOKEN" --groups "tester_group"' 
        }
    }
   }
  }

