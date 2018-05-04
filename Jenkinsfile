pipeline {
  agent any
  stages {
    stage('checkout') {
      steps {
        git 'https://github.com/amalic/xml2rdf.git'
      }
    }
    stage('build') {
      steps {
        sh 'docker build -t xml2rdf:latest .'
      }
    }
  }
}