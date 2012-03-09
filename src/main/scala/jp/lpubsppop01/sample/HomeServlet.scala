package jp.lpubsppop01.sample

import java.io._
import java.net._
import java.util._

import javax.servlet.http._

class HomeServlet extends HttpServlet {

  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    var session = request.getSession
    val accessToken = session.getAttribute(OAuthClientServlet.AccessTokenAttrId)
    val html =
      if (accessToken != null) {
        "<html><body><h1>Authorized</h1></body></html>"
      } else {
        "<html><body><h1>Not Authorized</h1><a href=\"/oauth\">oauth</a></body></html>"
      }

    response.setContentType("text/html")
    var out = response.getWriter
    out.print(html)
  }

}
