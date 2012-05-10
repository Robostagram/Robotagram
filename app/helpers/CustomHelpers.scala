package helpers

import views.html.customBootStrapFieldTemplate
import views.html.helper.FieldConstructor

object CustomHelpers {

  // so that you can just add
  //   @import helpers.CustomHelpers._
  // in a template and form controls will use that custom template
  // useful to display form errors/form validation "à la bootstrap"
  implicit val myFields = FieldConstructor(customBootStrapFieldTemplate.f) // .f is the template function ( params => Html )?

}
