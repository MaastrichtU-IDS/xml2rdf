pipeline {
  agent any
  stages {
    stage('checkout') {
      steps {
        git 'https://github.com/amalic/xml2rdf.git'
      }
    }
    stage('') {
      steps {
        sh 'docker build -t rdf-upload:latest .'
      }
    }
  }
}