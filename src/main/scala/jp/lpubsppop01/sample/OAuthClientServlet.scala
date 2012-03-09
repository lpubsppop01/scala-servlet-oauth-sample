package jp.lpubsppop01.sample

import java.io._
import java.net._
import java.util._

import javax.servlet.http._

import net.oauth._
import net.oauth.client._
import net.oauth.client.httpclient4._

class OAuthClientServlet extends HttpServlet {

  import OAuthClientServlet._

  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    var session = request.getSession
    var consumer = getConsumer(session)

    var client = new OAuthClient(new HttpClient4())
    var accessor = new OAuthAccessor(consumer)

    var requestToken = session.getAttribute(RequestTokenAttrId).asInstanceOf[String]
    var requestTokenSecret = session.getAttribute(RequestTokenSecretAttrId).asInstanceOf[String]
    if (requestToken == null || requestTokenSecret == null) {
      onFirstStep(request, response, session, consumer, client, accessor)
    } else {
      onSecondStep(request, response, session, client, accessor, requestToken, requestTokenSecret)
    }
  }

  private def getConsumer(session: HttpSession): OAuthConsumer = {
    def createConsumer(): OAuthConsumer = {
      var loader = getClass.getClassLoader
      val url = loader.getResource(ConsumerResourceName)
      val props = ConsumerProperties.getProperties(url)

      val consumers = new ConsumerProperties(props)
      var consumer = consumers.getConsumer(ConsumerPropId)
      val scope = props.getProperty(ScopePropId)
      if (scope != null)
        consumer.setProperty(ScopePropId, scope)
      consumer
    }

    var consumer = session.getAttribute(ConsumerAttrId).asInstanceOf[OAuthConsumer]
    if (consumer == null) {
      consumer = createConsumer
      session.setAttribute(ConsumerAttrId, consumer)
    }
    consumer
  }

  private def onFirstStep(
    request: HttpServletRequest,
    response: HttpServletResponse,
    session: HttpSession,
    consumer: OAuthConsumer,
    client: OAuthClient,
    accessor: OAuthAccessor
  ): Unit = {
    var params = new HashMap[Object, Object]()
    val callbackURL = request.getRequestURL.toString
    params.put(OAuth.OAUTH_CALLBACK, callbackURL)
    val scope = consumer.getProperty(ScopePropId)
    if (scope != null)
      params.put(ScopeParamId, scope)
    client.getRequestToken(accessor, "GET", params.entrySet)
    val requestToken = accessor.requestToken
    val requestTokenSecret = accessor.tokenSecret

    val authUrl = accessor.consumer.serviceProvider.userAuthorizationURL
    val url = OAuth.addParameters(authUrl, OAuth.OAUTH_TOKEN, requestToken)

    session.setAttribute(RequestTokenAttrId, requestToken)
    session.setAttribute(RequestTokenSecretAttrId, requestTokenSecret)

    response.sendRedirect(url)
  }

  private def onSecondStep(
    request: HttpServletRequest,
    response: HttpServletResponse,
    session: HttpSession,
    client: OAuthClient,
    accessor: OAuthAccessor,
    requestToken: String,
    requestTokenSecret: String
  ): Unit = {
    val verifier = request.getParameter(OAuth.OAUTH_VERIFIER)
    accessor.tokenSecret = requestTokenSecret
    var params = new HashMap[Object, Object]()
    params.put(OAuth.OAUTH_TOKEN, requestToken)
    params.put(OAuth.OAUTH_VERIFIER, verifier)

    client.getAccessToken(accessor, "GET", params.entrySet)

    val accessToken = accessor.accessToken
    val accessTokenSecret = accessor.tokenSecret
    session.removeAttribute(ConsumerAttrId)
    session.removeAttribute(RequestTokenAttrId)
    session.removeAttribute(RequestTokenSecretAttrId)
    session.setAttribute(AccessTokenAttrId, accessToken)
    session.setAttribute(AccessTokenSecretAttrId, accessTokenSecret)

    response.sendRedirect("/")
  }

}

object OAuthClientServlet {

  val ConsumerResourceName = "consumer.properties"
  val ConsumerPropId = "googleKey"
  val ScopePropId = "googleKey.scope"

  val ScopeParamId = "scope"

  val ConsumerAttrId = "oauth_consumer"
  val RequestTokenAttrId = "oauth_request_token"
  val RequestTokenSecretAttrId = "oauth_request_token_secret"
  val AccessTokenAttrId = "oauth_request_token"
  val AccessTokenSecretAttrId = "oauth_request_token_secret"

}
