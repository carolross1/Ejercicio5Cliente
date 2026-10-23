pipeline {
    agent any

    environment {
        JAVA_HOME = '/opt/java/openjdk'
        PATH = "/opt/maven/bin:${env.PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                echo '=== [1/4] Descargando código del Frontend ==='
                checkout scm
            }
        }

        stage('Compilación y Pruebas (Java 21)') {
            steps {
                echo '=== [2/4] Compilando con JDK 21 y ejecutando pruebas unitarias ==='
                sh 'java -version'
                sh 'mvn -version'
                sh 'mvn clean test'
            }
        }

        stage('Verificación de Cobertura JaCoCo') {
            steps {
                echo '=== [3/4] Validando reporte JaCoCo ==='
                publishHTML(target: [
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target/site/jacoco',
                    reportFiles: 'index.html',
                    reportName: 'Reporte JaCoCo Frontend'
                ])
            }
        }

        stage('Empaquetado') {
            steps {
                echo '=== [4/4] Empaquetando JAR para Java 21 ==='
                sh 'mvn package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
        }
        success {
            echo ' Pipeline de Frontend finalizado con éxito en Java 21.'
        }
        failure {
            echo '❌ Error en el Pipeline de Frontend.'
        }
    }
}
