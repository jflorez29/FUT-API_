package ea.sports.fut.model

data class Persona (val personaId : Int,
                    val personaName : String,
                    val returningUser : Int,
                    val onlineAccess : Boolean,
                    val trial : Boolean,
                    val userState : String,
                    val userClubList: MutableList<Club>,
                    val trialFree : Boolean,
                    val skuAccessList: Int)

data class Club(val year : String,
                val assetId : Int,
                val teamId : Int,
                val lastAccessTime : Int,
                val platform : String,
                val clubName : String,
                val clubAbbr : String,
                val established : Int,
                val divisionOnline : Int,
                val badgeId : Int)