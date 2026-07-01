package dev.yash.warrantywise.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import dev.yash.warrantywise.model.Product
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val uid get() = auth.currentUser?.uid ?: ""

    private fun userProducts() = firestore.collection("users").document(uid).collection("products")

    fun getProducts(): Flow<List<Product>> = callbackFlow {
        val listener = userProducts()
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toObject(Product::class.java) } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun addProduct(product: Product, invoiceUri: Uri?): Result<String> = try {
        val docRef = userProducts().document()
        val invoiceUrl = invoiceUri?.let { uploadInvoice(docRef.id, it) } ?: ""
        docRef.set(
            product.copy(
                productId = docRef.id,
                userId = uid,
                invoiceImageUrl = invoiceUrl,
                createdAt = System.currentTimeMillis()
            )
        ).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateProduct(product: Product, newInvoiceUri: Uri?): Result<Unit> = try {
        val invoiceUrl = newInvoiceUri?.let { uploadInvoice(product.productId, it) } ?: product.invoiceImageUrl
        userProducts().document(product.productId).set(product.copy(invoiceImageUrl = invoiceUrl)).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteProduct(productId: String): Result<Unit> = try {
        userProducts().document(productId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getProduct(productId: String): Product? = try {
        userProducts().document(productId).get().await().toObject(Product::class.java)
    } catch (e: Exception) {
        null
    }

    private suspend fun uploadInvoice(productId: String, uri: Uri): String {
        val ref = storage.reference.child("invoices/$uid/$productId/${UUID.randomUUID()}.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}
