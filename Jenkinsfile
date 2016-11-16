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

    stage("Build") {
        image = docker.build("edustor/core:ci-$env.BUILD_NUMBER")
    }

    if (env.BRANCH_NAME == "master") {
        stage("Push"){
            docker.withRegistry(REGISTRY_URL, REGISTRY_CREDENTIALS) {
                image.push("latest")
            }
        }

        stage("Deploy"){
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