package ea.sports.fut

import ea.sports.fut.model.*

interface CoreI {

    fun getPlayers() : List<Player>

    fun bid(auctionInfo: AuctionInfo, fast: Boolean = false, bid : Long) : Boolean

    fun search(type : TypeCard, level : LevelCard? = null, category: CategoryConsumables? = null, assetId : Int? = null, minPrice : Int? = null,
               maxPrice : Int? = null, minBuy : Int? = null, maxBuy : Int? = null, league : Int? = null, club : Int? = null,
               position : String? = null, nationality : Int? = null, rare : Boolean? = null, playStyle : String? = null,
               start : Int = 0, pageSize : Int = 20) : List<AuctionInfo>

    fun tradeStatus(tradeId: List<AuctionInfo>): List<AuctionInfo>

    fun tradePile() : List<AuctionInfo>

    fun watchList() : List<AuctionInfo>
}