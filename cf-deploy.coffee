module.exports = (cfDeploy) ->
    isProduction = cfDeploy.args.smokeTest is 'prod'

    domain: if not isProduction then 'mcf-np.local'
    deployer: cfDeploy.deployers.awsDeployment
    deployable: "target/universal/sequential*.zip"
    route: 'sequential'
    memoryLimit: '1G'
    environment:
        APP_URL_BASE: process.env.APP_URL_BASE
        OAUTH_SECRET_ACCESS_KEY: process.env.OAUTH_SECRET_ACCESS_KEY
        OAUTH_ACCESS_KEY_ID: process.env.OAUTH_ACCESS_KEY_ID
        OAUTH_URL: process.env.OAUTH_URL
        localDevelopment: false
    services: [
        "sequential-db"
    ]
    smokeTest: 'nonProd'
