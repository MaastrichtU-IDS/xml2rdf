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
        sh 'docker build --no-cache -t xml2rdf:latest .'
      }
    }
  }
}