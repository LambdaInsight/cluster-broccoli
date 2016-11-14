package de.frosner.broccoli.controllers

import de.frosner.broccoli.services.{BuildInfoService, InstanceService, PermissionsService, SecurityService}
import jp.t2v.lab.play2.auth.test.Helpers._
import org.mockito.Mockito._
import org.specs2.matcher.MatchResult
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.{Action, AnyContent, Result}
import play.api.test.{FakeRequest, PlaySpecification, _}

import scala.concurrent.Future

class AboutControllerSpec extends PlaySpecification with ServiceMocks with AuthUtils {

  sequential // http://stackoverflow.com/questions/31041842/error-with-play-2-4-tests-the-cachemanager-has-been-shut-down-it-can-no-longe

  "about" should {

    "return the about object" in {
      testWithAllAuths(UserAccount("frank", "pass")) {
        securityService =>
          AboutController(
            buildInfoService = withDummyValues(mock(classOf[BuildInfoService])),
            instanceService = withEmptyInstancePrefix(mock(classOf[InstanceService])),
            permissionsService = withDefaultPermissionsMode(mock(classOf[PermissionsService])),
            securityService = securityService
          )
      } {
        controller => controller.about
      } {
        identity
      } {
        (controller, result) => (status(result) must be equalTo 200) and {
          contentAsJson(result) must be equalTo JsObject(Map(
            "project" -> JsObject(Map(
              "name" -> JsString(controller.buildInfoService.projectName),
              "version" -> JsString(controller.buildInfoService.projectVersion)
            )),
            "scala" -> JsObject(Map(
              "version" -> JsString(controller.buildInfoService.scalaVersion)
            )),
            "sbt" -> JsObject(Map(
              "version" -> JsString(controller.buildInfoService.sbtVersion)
            )),
            "permissions" -> JsObject(Map(
              "mode" -> JsString(controller.permissionsService.getPermissionsMode())
            )),
            "nomad" -> JsObject(Map(
              "jobPrefix" -> JsString(controller.instanceService.nomadJobPrefix)
            ))
          ))
        }
      }
    }

  }

}