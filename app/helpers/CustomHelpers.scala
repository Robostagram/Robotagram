package helpers

import views.html.customBootStrapFieldTemplate
import views.html.helper.FieldConstructor

/**
 * Created with IntelliJ IDEA.
 * User: tibal
 * Date: 08/05/12
 * Time: 08:01
 * To change this template use File | Settings | File Templates.
 */

object CustomHelpers {
  implicit val myFields = FieldConstructor(customBootStrapFieldTemplate.f)
}
