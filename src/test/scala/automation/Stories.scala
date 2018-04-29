package automation

import java.io.File
import java.util

import com.mohaine.util.StreamUtils
import sequential._
import org.apache.http.client.methods._
import org.apache.http.impl.client.HttpClientBuilder
import org.jbehave.core.annotations._
import org.jbehave.core.configuration.{Configuration, MostUsefulConfiguration}
import org.jbehave.core.failures.FailingUponPendingStep
import org.jbehave.core.junit.JUnitStories
import org.jbehave.core.reporters.{Format, StoryReporterBuilder}
import org.junit.Assert._


abstract class Stories extends JUnitStories {
  override def configuration(): Configuration = {
    new MostUsefulConfiguration()
      .useStoryReporterBuilder(new StoryReporterBuilder().withFormats(Format.ANSI_CONSOLE, Format.STATS, Format.HTML))
      .usePendingStepStrategy(new FailingUponPendingStep())

  }
  configuredEmbedder().embedderControls().doGenerateViewAfterStories(true)
    .doIgnoreFailureInView(true).useThreads(1)
  configuredEmbedder().useMetaFilters(util.Arrays.asList("-skip"))
}

class WebAppSteps extends Server {

  val client = HttpClientBuilder.create().build()
  val status = new Status()
  val baseUrl = "http://localhost:8080/sequential"

  trait Response {
    def contentAsString: String
    def statusCode: Int
    def close: Unit
  }

  class Status() {
    var lastResponse: Option[Response] = None
  }

  @BeforeScenario
  def writeStoryDurations(): Unit = {
    // JBehave fails if this file is missing....
    val file = new File("target/jbehave/storyDurations.props")
    if (!file.exists()) {
      file.getParentFile.mkdirs()
      file.createNewFile()
    }
  }

  @Given("the server is started")
  def serverStarted(): Unit = {
    val get = new HttpGet(baseUrl)
    executeNext(get)
  }

  @Then("the response code should be $code")
  def validateResponseCode(code: Int) {
    assertNotNull(code)
    assertTrue("Have a response", status.lastResponse.isDefined)
    assertEquals(s"Response code should have been '$code'", code, status.lastResponse.get.statusCode)
  }

  @Then("the response text should contain $text")
  def validateResponseTextContain(text: String) {
    val response = status.lastResponse.get.contentAsString
    try {
      assertNotNull(text)
      assertTrue("Have a response", status.lastResponse.isDefined)
      assertTrue(s"Should contain '$text'", response.contains(text))
    } finally {
      if (!response.contains(text)) {
        log.info("***************************************")
        log.info(status.lastResponse.get.contentAsString)
        log.info("***************************************")
      }
    }
  }

  @Then("the response text should not contain $text")
  def validateResponseTextNotContain(text: String) {
    assertNotNull(text)
    assertTrue("Have a response", status.lastResponse.isDefined)
    assertFalse(s"Should contain '$text'", status.lastResponse.get.contentAsString.contains(text))
  }

  protected def executeNext(req: HttpUriRequest): Unit = {
    if (status.lastResponse.isDefined) {
      status.lastResponse.get.close
    }
    val response: CloseableHttpResponse = client.execute(req)
    status.lastResponse = Some(new Response() {
      override lazy val contentAsString: String = {
        new String(StreamUtils.readStream(response.getEntity.getContent))
      }

      override def statusCode: Int = {
        response.getStatusLine.getStatusCode
      }

      override def close: Unit = {
        response.close()
      }
    })

  }
}
