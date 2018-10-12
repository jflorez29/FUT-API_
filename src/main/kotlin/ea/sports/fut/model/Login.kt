package ea.sports.fut.model

import utils.Constants

data class Login (val email : String,
                  val password : String,
                  var secretAnswer : String,
                  val platform : Platform,
                  val code : String? = null,
                  val timeout : Int = Constants.timeout,
                  val delay : Int = Constants.delay,
                  val typeCodeConfirmation : TypeCodeConfirmation = TypeCodeConfirmation.SMS)