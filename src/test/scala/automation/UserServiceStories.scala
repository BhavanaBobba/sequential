package automation

import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.entity.StringEntity
import org.jbehave.core.annotations.When
import org.jbehave.core.configuration.scala.ScalaContext
import org.jbehave.core.io.{CodeLocations, StoryFinder}
import org.jbehave.core.model.ExamplesTable
import org.jbehave.core.steps.InjectableStepsFactory
import org.jbehave.core.steps.scala.ScalaStepsFactory

import scala.collection.JavaConverters


class ReferenceDataServiceStories extends Stories {

  override def storyPaths(): java.util.List[String] = {
    new StoryFinder().findPaths(CodeLocations.codeLocationFromClass(this.getClass()),"/UserService*.story", "")
  }

  override def stepsFactory(): InjectableStepsFactory = {
    new ScalaStepsFactory(configuration(), new ScalaContext(classOf[ReferenceDataServiceSteps].getName))
  }

}

class ReferenceDataServiceSteps extends WebAppSteps {

  @When("I call get all users")
  def getAllUsers = {
    val get = new HttpGet(baseUrl + "/users")
    executeNext(get)
  }
}