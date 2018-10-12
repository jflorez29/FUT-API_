package utils

import com.google.api.core.ApiFuture
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import com.google.gson.Gson
import ea.sports.fut.model.AuctionInfo
import ea.sports.fut.model.firestore.Authentication
import ea.sports.fut.model.firestore.Player
import ea.sports.fut.model.firestore.User
import mu.KLogging
import java.io.FileInputStream


class FirebaseUtil {

    companion object : KLogging()
    private val db : Firestore

    private val COLLECTION_PLAYERSLIST = "playerList"
    private val COLLECTION_PLAYERS = "PLAYERS"
    private val COLLECTION_EA = "EA"

    init {
        val serviceAccount = FileInputStream(javaClass.classLoader.getResource("firebase/fut-api.json").file)
        val credentials = GoogleCredentials.fromStream(serviceAccount)
        val options = FirebaseOptions.Builder()
                .setCredentials(credentials)
                .build()
        FirebaseApp.initializeApp(options)
        db = FirestoreClient.getFirestore()
    }

    fun updatePlayersEA(list : MutableList<ea.sports.fut.model.Player>){
        list.forEach { player ->
            val future : ApiFuture<WriteResult> = db.collection(COLLECTION_EA)
                    .document(COLLECTION_PLAYERS)
                    .collection(COLLECTION_PLAYERS)
                    .document("${player.id} - ${player.firstName} ${player.lastName}")
                    .set(player)
        }


    }

    fun updateUser(user : User){
        if (getUserDocument(user.email).auth.token != "null"){
            updateUserDocument(user)
        }else{
            createUserDocument(user)
        }
    }

    private fun updateUserDocument(user: User) {
        val future = db.collection("users").document(user.email).set(user)
        logger.info { "Updating doc ${user.email}  -> ${future.get().updateTime}" }
    }

    private fun createUserDocument(user : User){
        val future = db.collection("users").document(user.email).set(user)
        logger.info { "Creating doc ${user.email}  -> ${future.get().updateTime}" }
    }

    fun insertPurchase(auctionInfo: AuctionInfo, user: User){
        val future : ApiFuture<DocumentReference>? = db.collection("purchases").document(user.email).collection(Constants.TODAY).add(auctionInfo)
        logger.info { "Inserting purchase ${auctionInfo.tradeId} " }
    }

    fun insertPlayer(player : Player, user: User){
        val future : ApiFuture<WriteResult> = db.collection(COLLECTION_PLAYERSLIST)
                .document(user.email)
                .collection(COLLECTION_PLAYERS)
                .document("${player.name} - ${player.assetId}")
                .set(player)
        logger.info { "Creating doc ${player.name} - ${player.assetId}  -> ${future.get().updateTime}" }
    }

    fun getUserDocument(user : String) : User {
        val docRef  = db.collection("users").document(user)
        val future = docRef.get()
        val document : DocumentSnapshot = future.get()
        if (document.data == null) {
            return User(user, Authentication("null", ""))
        } else {
            val json = Gson().toJson(document.data)
            return Gson().fromJson(json, User::class.java)
        }
    }


    fun getPlayerList(user: User) : MutableList<Player>{
        val list = arrayListOf<Player>()
        val docRef  = db.collection(COLLECTION_PLAYERSLIST)
                .document(user.email)
                .collection(COLLECTION_PLAYERS)
                .whereEqualTo("state", true)
                .whereGreaterThan("quantity" , 0)

        val future = docRef.get()
        val documents : QuerySnapshot = future.get()
        if (!documents.isEmpty){
            for (document in documents) {
                val json = Gson().toJson(document.data)
                var player = Gson().fromJson(json, Player::class.java)
                list.add(player)
            }
        }
        return list
    }


}