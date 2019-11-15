package com.nikhiljain.otpreader.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import android.telephony.SmsMessage.FORMAT_3GPP
import android.telephony.SmsMessage.FORMAT_3GPP2
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.PHONE_TYPE_CDMA
import com.nikhiljain.otpreader.R
import com.nikhiljain.otpreader.callback_interfaces.Common

/**
 * @author Nikhil Jain
 *
 * Broadcast Receiver to listen to upcoming messages
 *
 * <p>
 * This class receives sms whenever a new sms is received on the
 * device and then collect pdu array (Protocol Data Unit i.e format for sms)
 * and then prepare SMS Message from this array.
 *
 * The Otp Listener delegate can be implemented by activity to get message
 * for their use.
 * </p>
 *
 * <div class="special reference">
 * <h3>Developer Guides</h3>
 * <p>For more information about using BroadcastReceiver, read the
 * <a href="https://medium.com/@STYFI_STYLABS/automatically-read
 * -otp-from-smses-android-4-3-to-8-x-99e1f75b5804">Automatically-read-OTP
 * from SMSes — Android 4.3 to 8.x</a> Medium Link.</p></div>
 */
class SMSListener : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val smsMessage = getSmsMessageFromIntent(context = context, intent = intent)
        if (mListener != null) mListener!!.onOTPReceived(
            "Sender : ${smsMessage?.originatingAddress} " +
                    "\nMessage Body : ${smsMessage?.messageBody}"
        )
    }

    private fun getSmsMessageFromIntent(context: Context, intent: Intent): SmsMessage? {
        val data = intent.extras
        val protocolDataUnitArray: Array<Any>? =
            data?.get(context.getString(R.string.PDU_intent_key)) as Array<Any>?

        val format = getFormatForDevice(context)

        if (protocolDataUnitArray != null) {
            for (unit in protocolDataUnitArray) {
                return extractMessageFromPDU(unit as ByteArray, format)
            }
        }
        return null
    }

    private fun getFormatForDevice(context: Context): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null

        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val activePhone = telephonyManager.phoneType
        return if (PHONE_TYPE_CDMA == activePhone) FORMAT_3GPP2 else FORMAT_3GPP
    }

    private fun extractMessageFromPDU(unit: ByteArray, format: String?): SmsMessage {
        var formatOfMsg = format

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return SmsMessage.createFromPdu(unit)
        }
        if (formatOfMsg == null) {
            formatOfMsg = "3gpp"
        }
        return SmsMessage.createFromPdu(unit, formatOfMsg)
    }

    companion object {
        private var mListener: Common.OTPListener? =
            null

        fun bindListener(listener: Common.OTPListener) {
            mListener = listener
        }

        fun unbindListener() {
            mListener = null
        }
    }
}