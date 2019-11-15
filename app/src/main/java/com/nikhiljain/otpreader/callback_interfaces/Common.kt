package com.nikhiljain.otpreader.callback_interfaces

interface Common {
    interface OTPListener {
        fun onOTPReceived(otp: String)
    }
}