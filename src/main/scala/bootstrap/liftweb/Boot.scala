package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._

import common._
import http._
import sitemap._
import Loc._
import sun.nio.cs.StandardCharsets
import org.apache.commons.codec.CharEncoding


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    // where to search snippet
    LiftRules.addToPackages("com.micronautics.meetupRoll.web")

    // Build SiteMap
//    val entries = Menu(Loc("Home", List("index"), "Home")) :: Nil

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
//    LiftRules.setSiteMap(SiteMap(entries:_*))

//    LiftRules.ajaxStart =
//      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
//
//    LiftRules.ajaxEnd =
//      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding(CharEncoding.UTF_8))

    LiftRules.siteMapFailRedirectLocation = List("/")

    LiftRules.exceptionHandler.prepend {
      case (_, _, exception) => {
        exception.printStackTrace()
        RedirectResponse("/")
      }
    }
  }
}
