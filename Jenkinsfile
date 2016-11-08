node {
    properties([parameters([
            string(defaultValue: 'https://registry.wutiarn.ru', description: '', name: 'REGISTRY_URL'),
            [$class: 'CredentialsParameterDefinition', credentialType: 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl', defaultValue: 'registry.wutiarn.ru', description: '', name: 'REGISTRY_CREDENTIALS', required: true],
            [$class: 'CredentialsParameterDefinition', credentialType: 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl', defaultValue: 'api.rancher.wutiarn.ru', description: '', name: 'RANCHER_API_CREDENTIALS', required: true],
            string(defaultValue: '1e4', description: '', name: 'RANCHER_STACK_ID'),
            string(defaultValue: 'edustor', description: '', name: 'RANCHER_SERVICE_NAME'),
            string(defaultValue: 'edustor', description: '', name: 'RANCHER_STACK_NAME'),
            string(defaultValue: 'https://rancher.wutiarn.ru/v1/projects/1a5', description: '', name: 'RANCHER_URL')
    ]), pipelineTriggers([])])

    stage("Checkout") {
        checkout scm
    }

    stage("Build") {
        image = docker.build("edustor/core")
    }

    if (env.BRANCH_NAME == "master") {
        stage("Push"){
            docker.withRegistry(env.REGISTRY_URL, env.REGISTRY_CREDENTIALS) {
                image.push()
            }
        }

        stage("Deploy"){
            docker.image("wutiarn/rancher-deployer").inside {
                withCredentials([[$class          : 'UsernamePasswordMultiBinding', credentialsId: env.RANCHER_API_CREDENTIALS,
                                  usernameVariable: 'ACCESS_KEY', passwordVariable: 'SECRET_KEY']]) {
                    env.RANCHER_ACCESS_KEY = ACCESS_KEY
                    env.RANCHER_SECRET_KEY = SECRET_KEY
                }

                sh "/root/upgrade.sh"
            }
        }
    }
}