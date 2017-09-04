package de.frosner.broccoli.services

import com.mohiva.play.silhouette.api.util.Credentials
import de.frosner.broccoli.auth.{Account, AuthConfiguration, AuthMode, Role}
import org.specs2.mutable.Specification

import scala.concurrent.duration.Duration

class SecurityServiceSpec extends Specification {

  def configWithAccounts(accounts: Seq[Account]): AuthConfiguration =
    AuthConfiguration(
      mode = AuthMode.Conf,
      session = AuthConfiguration.Session(timeout = Duration(1, "hour"), allowMultiLogin = true),
      cookie = AuthConfiguration.Cookie(secure = true),
      conf = AuthConfiguration.Conf(
        accounts = accounts
          .map(
            a =>
              AuthConfiguration.ConfAccount(
                a.name,
                a.password,
                a.instanceRegex,
                a.role
            ))
          .toList),
      allowedFailedLogins = 3
    )

  val account = Account("frank", "pass", "^test.*", Role.Administrator)

  "An authentication check" should {

    "succeed if the account matches" in {
      SecurityService(configWithAccounts(List(account)))
        .isAllowedToAuthenticate(Credentials(account.name, account.password)) === true
    }

    "fail if the username does not exist" in {
      SecurityService(configWithAccounts(List(account)))
        .isAllowedToAuthenticate(Credentials("new", account.password)) === false
    }

    "fail if the password does not matche" in {
      SecurityService(configWithAccounts(List(account)))
        .isAllowedToAuthenticate(Credentials(account.name, "new")) === false
    }

    "succeed if the number of failed logins is equal to the allowed ones" in {
      val failedCredentials = Credentials(account.name, password = "new")
      val service = SecurityService(configWithAccounts(List(account)))
      val failedAttempts = for (attemptNo <- 1 to service.allowedFailedLogins) {
        service.isAllowedToAuthenticate(failedCredentials)
      }
      service.isAllowedToAuthenticate(Credentials(account.name, account.password)) === true
    }

    "fail if the number of failed logins is greater than the allowed number" in {
      val failedCredentials = Credentials(account.name, password = "new")
      val service = SecurityService(configWithAccounts(List(account)))
      val failedAttempts = for (attemptNo <- 0 to service.allowedFailedLogins) {
        service.isAllowedToAuthenticate(failedCredentials)
      }
      service.isAllowedToAuthenticate(Credentials(account.name, account.password)) === false
    }

  }

  "Finding accounts by id" should {

    "find an existing account" in {
      SecurityService(configWithAccounts(List(account)))
        .getAccount(account.name) === Some(account)
    }

    "not return anything if the name does not exist" in {
      SecurityService(configWithAccounts(List(account)))
        .getAccount("notExisting") === None
    }

  }

}
