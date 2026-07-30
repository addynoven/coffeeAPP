package com.example.testing1.util

import android.app.Activity
import android.util.Log
import com.example.testing1.R
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

sealed class PaymentResult {
    data class Success(val paymentId: String?, val paymentData: PaymentData?) : PaymentResult()
    data class Error(val code: Int, val description: String?, val paymentData: PaymentData?) : PaymentResult()
}

@Singleton
class RazorpayManager @Inject constructor() : PaymentResultWithDataListener {

    private val _paymentEvents = MutableSharedFlow<PaymentResult>(extraBufferCapacity = 1)
    val paymentEvents: SharedFlow<PaymentResult> = _paymentEvents.asSharedFlow()

    fun startPayment(
        activity: Activity,
        amountInRupees: Double,
        userEmail: String?,
        userName: String?,
        userPhone: String = "9876543210"
    ) {
        val checkout = Checkout()
        val keyId = activity.getString(R.string.razorpay_key_id)
        checkout.setKeyID(keyId)

        try {
            val amountInPaise = (amountInRupees * 100).toLong()
            val options = JSONObject().apply {
                put("name", "Coffee App")
                put("description", "Order Payment")
                put("currency", "INR")
                put("amount", amountInPaise)
                put("theme.color", "#C67C4E") // Matching app primary coffee theme

                val prefill = JSONObject().apply {
                    put("email", if (!userEmail.isNull_orEmpty()) userEmail else "customer@coffeeapp.io")
                    put("contact", userPhone)
                    if (!userName.isNull_orEmpty()) {
                        put("name", userName)
                    }
                }
                put("prefill", prefill)

                val retry = JSONObject().apply {
                    put("enabled", true)
                    put("max_count", 2)
                }
                put("retry", retry)
            }

            Checkout.preload(activity.applicationContext)
            checkout.open(activity, options)
        } catch (e: Exception) {
            Log.e("RazorpayManager", "Error starting Razorpay checkout: ${e.message}", e)
            _paymentEvents.tryEmit(PaymentResult.Error(-1, e.message ?: "Initialization failed", null))
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        Log.d("RazorpayManager", "Payment Success: $razorpayPaymentId")
        _paymentEvents.tryEmit(PaymentResult.Success(razorpayPaymentId, paymentData))
    }

    override fun onPaymentError(code: Int, description: String?, paymentData: PaymentData?) {
        Log.e("RazorpayManager", "Payment Error: code=$code desc=$description")
        _paymentEvents.tryEmit(PaymentResult.Error(code, description, paymentData))
    }
}
private fun String?.isNull_orEmpty(): Boolean = this == null || this.isEmpty()
