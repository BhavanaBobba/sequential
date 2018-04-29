# Gatling
1. Place `GATLING_URL=***********************` in your `.env` file. Keep in mind that the URL should be the Akana URL.
1. Run the API using `sbt run`
1. To run gatling use `sbt gatling:test`

## Gatling SBT plugin
Find [here](http://gatling.io/docs/current/extensions/sbt_plugin/). Use the [Gatling Documentation](http://gatling.io/docs/2.2/) 
page to read up on the tool. It's recommended to read through the [advance tutorial](http://gatling.io/docs/current/advanced_tutorial/)
because the code takes some ideas from it.

A good [tutorial](https://sysgears.com/articles/restful-service-load-testing-using-gatling-2/) to read through.

### Excludes gatling package in test directory from `scoverage`
Include
`coverageExcludedPackages := "sequential.gatling.*"` in the `build.sbt` file for `scoverage`.

## Dependencies
* `gatling-charts-highcharts`
* `gatling-test-framework`
* `scalaj-http`