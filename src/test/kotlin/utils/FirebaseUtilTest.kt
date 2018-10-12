package utils

import ea.sports.fut.model.firestore.Authentication
import ea.sports.fut.model.firestore.Player
import ea.sports.fut.model.firestore.User
import org.junit.Test

class FirebaseUtilTest {

    @Test
    fun getPlayerList() {
        val firebase : FirebaseUtil = FirebaseUtil()

        val list = firebase.getPlayerList(user = User("fga.jeisson@gmail.com", Authentication("", "")))
        println(list)
    }


    @Test
    fun insertPlayer(){
        val firebase : FirebaseUtil = FirebaseUtil()
        firebase.insertPlayer(
                Player(238436, 330000, "Matthaus 88", false, quantity = 1),
                User("fga.jeisson@gmail.com", Authentication("", "")))
    }
}