package com.nikhiljain.otpreader

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nikhiljain.otpreader.broadcast_receivers.SMSListener
import com.nikhiljain.otpreader.callback_interfaces.Common

typealias Completion = (Boolean) -> Unit

class MainActivity : AppCompatActivity(), Common.OTPListener {

    private val tag = "MainActivity"

    private var tvDisplayMessage: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViews()
        checkReadSmsPermission()
    }

    private fun findViews() {
        tvDisplayMessage = findViewById(R.id.tv_display_msg)
    }

    private fun checkReadSmsPermission() {
        if (isReadSmsPermissionDenied()) {
            if (!shouldShowPermission()) {
                requestReadSmsPermission()
                return
            }

            showPermissionInfoDialog { isGranted ->
                if (!isGranted) return@showPermissionInfoDialog
                requestReadSmsPermission()
            }
        }

        bindSmsListener()
        Log.e(tag, "checkReadPermissions is called" + isReadSmsPermissionDenied())
    }

    private fun requestReadSmsPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECEIVE_SMS),
            Constants.MY_PERMISSIONS_REQUEST_RECEIVE_SMS
        )
    }

    private fun showPermissionInfoDialog(isGranted: Completion) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(getString(R.string.permission_req))
        dialogBuilder.setMessage(getString(R.string.permission_string))
        dialogBuilder.setPositiveButton(getString(R.string.grant_option)) { _, _ ->
            isGranted(true)
        }
        dialogBuilder.setNegativeButton(getString(R.string.deny_option)) { _, _ ->
            isGranted(false)
        }
    }

    private fun shouldShowPermission(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.RECEIVE_SMS
        )
    }

    private fun isReadSmsPermissionDenied(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECEIVE_SMS
        ) != PackageManager.PERMISSION_GRANTED
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.MY_PERMISSIONS_REQUEST_RECEIVE_SMS) {
            if (grantResults.isEmpty()) {
                Log.e(tag, "grant results is empty")
                requestReadSmsPermission()
                return
            }

            Log.e(tag, "request permissions is called")
            //do your work

            bindSmsListener()
        }
    }

    private fun bindSmsListener() {
        SMSListener.bindListener(this)
    }

    override fun onOTPReceived(otp: String) {
        tvDisplayMessage!!.text = otp
    }

    override fun onDestroy() {
        super.onDestroy()
        SMSListener.unbindListener()
    }
}
