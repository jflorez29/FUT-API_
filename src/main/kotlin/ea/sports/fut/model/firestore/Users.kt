package ea.sports.fut.model.firestore


data class User (val email : String, var auth : Authentication, var cookies : List<Cookie>? = null, var club : FifaClub ? = null)

data class Authentication(val token : String, val type_token : String)

data class Cookie(val domain : String, val path : String, val expiry : Long? = null, val secure : Boolean? = null,
                  val hostOnly : Boolean? = null, val name : String, val value : String)

data class Player(val assetId : Long, val buyValue : Int, val name : String, val state : Boolean, var quantity : Int)

data class FifaClub(var personaId : Long, var personaName : String, var clubName : String, var clubAbbr : String, var draw : Long, var won : Long,
                var loss : Long, var credits : Long, var divisionOffline : Long, var divisionOnline : String)