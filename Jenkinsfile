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
        sh 'docker build --no-cache -t rdf-upload:latest .'
      }
    }
  }
}