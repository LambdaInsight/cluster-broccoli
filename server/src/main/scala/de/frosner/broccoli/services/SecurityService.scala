package de.frosner.broccoli.services

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.util.Credentials
import de.frosner.broccoli.auth.{Account, AuthConfiguration, AuthMode, Role}
import de.frosner.broccoli.conf
import de.frosner.broccoli.conf.IllegalConfigException
import play.api.Configuration

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

@Singleton()
case class SecurityService @Inject()(configuration: AuthConfiguration) {

  private val log = play.api.Logger(getClass)

  val sessionTimeoutInSeconds: Int = configuration.session.timeout.toSeconds.toInt

  val allowedFailedLogins: Int = configuration.allowedFailedLogins

  val authMode: AuthMode = configuration.mode

  val cookieSecure: Boolean = configuration.cookie.secure
  val allowMultiLogin: Boolean = configuration.session.allowMultiLogin

  private val accounts: Set[Account] =
    configuration.conf.accounts.map(a => Account(a.username, a.password, a.instanceRegex, a.role)).toSet

  @volatile
  private var failedLoginAttempts: Map[String, Int] = Map.empty

  // TODO store failed logins (reset on successful login) and only allowToAuthenticate if not blocked

  def isAllowedToAuthenticate(credentials: Credentials): Boolean = {
    val credentialsFailedLoginAttempts = failedLoginAttempts.getOrElse(credentials.identifier, 0)
    val allowed = if (credentialsFailedLoginAttempts <= allowedFailedLogins) {
      accounts.exists { account =>
        account.name == credentials.identifier && account.password == credentials.password
      }
    } else {
      log.warn(
        s"Credentials for '${credentials.identifier}' exceeded the allowed number of failed logins: " +
          s"$allowedFailedLogins (has $credentialsFailedLoginAttempts)")
      false
    }
    if (!allowed) {
      failedLoginAttempts = failedLoginAttempts.updated(credentials.identifier, credentialsFailedLoginAttempts + 1)
    }
    allowed
  }

  def getAccount(id: String): Option[Account] = accounts.find(_.name == id)

}
