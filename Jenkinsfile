node {

    REGISTRY_CREDENTIALS = "registry.wutiarn.ru"
    REGISTRY_URL = "https://registry.wutiarn.ru"
    env.RANCHER_URL = "https://rancher.wutiarn.ru/v1/projects/1a5"
    RANCHER_API_CREDENTIALS = "api.rancher.wutiarn.ru"

    env.RANCHER_STACK_NAME = "edustor"
    env.RANCHER_SERVICE_NAME = "edustor"
    env.RANCHER_STACK_ID = "1e4"

    stage("Checkout") {
        checkout scm
    }

    stage("Prepare")
    baseImage = docker.build("edustor/core-base", "-f Dockerfile.base .")

    dir = pwd().replace("/var/lib/jenkins/workspace", "/mnt/media/jenkins/workspace")
    buildImage = baseImage.inside("-v $dir:/code -v /mnt/media/jenkins/cache/.gradle:/root/.gradle") {
        sh "./gradlew clean"

        stage "Test"
        sh "./gradlew test"
        junit allowEmptyResults: true, testResults: 'build/test-results/test/*.xml'

        stage "Assemble"
        sh "./gradlew assemble"
        sh "mv build/dist/edustor.jar ."
        archiveArtifacts "edustor.jar"
    }

    if (env.BRANCH_NAME == "master") {

        stage("Build image") {
            image = docker.build("edustor/core:ci-$env.BUILD_NUMBER")
        }

        stage("Push") {
            docker.withRegistry(REGISTRY_URL, REGISTRY_CREDENTIALS) {
                image.push("latest")
            }
        }

        stage("Deploy") {
            docker.image("wutiarn/rancher-deployer").inside {
                withCredentials([[$class          : 'UsernamePasswordMultiBinding', credentialsId: RANCHER_API_CREDENTIALS,
                                  usernameVariable: 'ACCESS_KEY', passwordVariable: 'SECRET_KEY']]) {
                    env.RANCHER_ACCESS_KEY = ACCESS_KEY
                    env.RANCHER_SECRET_KEY = SECRET_KEY
                }

                sh "/root/upgrade.sh"
            }
        }
    }
}