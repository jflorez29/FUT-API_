import ea.sports.fut.Core
import ea.sports.fut.model.*
import ea.sports.fut.model.firestore.Authentication
import ea.sports.fut.model.firestore.Player
import ea.sports.fut.model.firestore.User

fun main(args : Array<String>) {


    var comprados = 0
    val core = Core(Login("fga.jeisson@gmail.com", "Snl=110731;", "bogota", Platform.PS4, typeCodeConfirmation = TypeCodeConfirmation.EMAIL))
    var start = 0
    /*while (comprados < 10) {
        if (start >= 200 ) start = 0
        var search = core.search(type = TypeCard.DEVELOPMENT, category = CategoryConsumables.FITNESS, start = start)
        if (!search.isEmpty()) {
            for (item: AuctionInfo in search) {
                if (item.itemData.rareflag == 1 && item.buyNowPrice <= 900) {
                    println(item)
                    if (core.bid(item, fast = true, bid = item.buyNowPrice)) {
                        comprados++
                    }
                }
            }
        }
        start +=20
    }*/

    var cantidad = 0
    val firebase = core.firebase

    var list = firebase.getPlayerList(user = User("fga.jeisson@gmail.com", Authentication("", "")))
    print(list)
    while (!list.isEmpty()) {
        if (cantidad > (31..44).shuffled().last()){
            println("Waiting ")
            kotlin.run { Thread.sleep((60..300).shuffled().last().toLong() * 1000) } // Minimum delay between request
            cantidad = 0
        }
        if (cantidad == 0) list = firebase.getPlayerList(user = User("fga.jeisson@gmail.com", Authentication("", "")))
        val size = list.size-1
        val player : Player = list[(0..size).shuffled().last()]

        var search = core.search(type = TypeCard.PLAYER, assetId = player.assetId.toInt(), maxBuy = player.buyValue)
        cantidad++
        if (!search.isEmpty()) {
            for (item: AuctionInfo in search) {
                    println(item)
                    if (core.bid(item, fast = true, bid = item.buyNowPrice)) {
                        println("Enjoy ${item.tradeId}")
                        player.quantity -=1
                        firebase.insertPlayer(player, User("fga.jeisson@gmail.com", Authentication("", "")))
                        cantidad = 0
                    }
            }
        }
    }
}






