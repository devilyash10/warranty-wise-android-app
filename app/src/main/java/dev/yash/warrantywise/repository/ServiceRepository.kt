package dev.yash.warrantywise.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dev.yash.warrantywise.model.ServiceHistory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val uid get() = auth.currentUser?.uid ?: ""

    private fun serviceCol(productId: String) =
        firestore.collection("users").document(uid)
            .collection("products").document(productId)
            .collection("serviceHistory")

    fun getServiceHistory(productId: String): Flow<List<ServiceHistory>> = callbackFlow {
        val listener = serviceCol(productId)
            .orderBy("serviceDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toObject(ServiceHistory::class.java) } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun addServiceRecord(productId: String, record: ServiceHistory): Result<Unit> = try {
        val docRef = serviceCol(productId).document()
        docRef.set(
            record.copy(
                serviceId = docRef.id,
                productId = productId,
                createdAt = System.currentTimeMillis()
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteServiceRecord(productId: String, serviceId: String): Result<Unit> = try {
        serviceCol(productId).document(serviceId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
