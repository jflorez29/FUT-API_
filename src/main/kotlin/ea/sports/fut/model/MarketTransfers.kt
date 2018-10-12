package ea.sports.fut.model

enum class TypeCard(val typeCard : String){
    PLAYER("player"),
    STAFF("staff"),
    CLUB("clubinfo"),
    DEVELOPMENT("development"),
    TRAINING("training")
}

enum class LevelCard(val levelCard : String){
    GOLD("gold"),
    BRONZE("bronze"),
    SILVER("silver")
}

enum class CategoryConsumables(val category : String){
    FITNESS("fitness")
}

data class AuctionInfo(val tradeId : Long,
                       val itemData: ItemData,
                       val tradeState: String,
                       val buyNowPrice: Long,
                       val currentBid: Long,
                       val watched: Boolean,
                       val offers: Long,
                       val bidState: String,
                       val startingBid: Long,
                       val confidenceValue: Long,
                       val expires: Long,
                       val sellerName: String,
                       val sellerEstablished: Long,
                       val sellerId: Long,
                       val tradeOwner: Boolean,
                       val tradeIdStr: String)

data class ItemData(val id : Long,
                    val timestamp : String,
                    val formation : String,
                    val untradeable : Boolean,
                    val assetId : Long,
                    val rating : Long,
                    val itemType : String,
                    val resourceId : Long,
                    val owners : Long,
                    val discardValue : Long,
                    val itemState : String,
                    val cardsubtypeid : Long,
                    val lastSalePrice : Long,
                    val morale : Long,
                    val fitness : Long,
                    val injuryType : String,
                    val injuryGames : Long,
                    val preferredPosition : String,
                    val training : Long,
                    val contract : Long,
                    val suspension : Long,
                    val teamid : Long,
                    val rareflag : Int,
                    val playStyle : Long,
                    val leagueId : Long,
                    val assists : Long,
                    val lifetimeAssists : Long,
                    val loyaltyBonus : Long,
                    val pile : Long,
                    val nation : Long,
                    val resourceGameYear : Long,
                    val cardassetid : String,
                    val weightrare : String,
                    val amount : String)

