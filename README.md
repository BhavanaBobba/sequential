# Product Availability
A sample Akka-HTTP application

## Documentation

Swagger docs are available at `/sequential/docs`

## Testing/Building/Packaging

### Starting the service locally
A `.env` file is required or the following environmental variables injected through your IDE of choice.

```
APP_URL_BASE=localhost:8080
localDevelopment=true
OAUTH_URL=********************
GATLING_URL=localhost:8080/sequential/
```

To run from bash use the following command;
```shell
sbt run
```

### Running build/tests
```shell
sbt testCoverage
```

This is an alias in `build.sbt` that will compile the application and generate:

- `target/scalasytle-result.xml`: Checkstyle-formatted linter results
- `target/test-reports/*.xml`: JUnit XML test reports
- `target/scala-2.11/coverage-report/cobertura.xml`: Cobertura-formatted
  coverage report.

All of which Jenkins can be configured to pick up & display on the project's
page.

### Versioning releases

This package uses the [sbt-release](https://github.com/sbt/sbt-release) plugin
to handle versioning.  To perform an interactive release, run `sbt release` and
then set the desired release version and subsequent snapshot version.

To automatically use the current snapshot version as the release version and
then increment the minor version for the next snapshot, run
`sbt 'release with-defaults'`.

Once this is done, there should be a new release version tag, which can be
verified by running `git tag` (remember to push tags with `git push --tags`).

### Packaging the service
See the SBT assembly plugin for more info. To package as zip file that can be
deployed to CloudFoundry, run:

```shell
git checkout v[VERSION]
sbt clean universal:packageBin
```
