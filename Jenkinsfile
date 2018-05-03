pipeline {
  agent any
  stages {
    stage('checkout') {
      steps {
        git 'https://github.com/amalic/xml2rdf.git'
      }
    }
    stage('error') {
      steps {
        sh 'docker build -t --no-cache rdf-upload:latest .'
      }
    }
  }
}