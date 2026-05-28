package com.lucidos.connectivity

import android.content.Context
import android.nfc.NfcAdapter
import android.util.Log

class NfcManager(private val context: Context) {
    private val TAG = "NfcManager"
    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)

    fun isNfcEnabled(): Boolean {
        return try {
            nfcAdapter?.isEnabled ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking NFC state", e)
            false
        }
    }

    fun setNfcEnabled(enabled: Boolean) {
        try {
            Log.d(TAG, "Setting NFC: $enabled")
            nfcAdapter?.let { adapter ->
                if (enabled) {
                    val method = adapter.javaClass.getMethod("enable")
                    method.invoke(adapter)
                } else {
                    val method = adapter.javaClass.getMethod("disable")
                    method.invoke(adapter)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set NFC state", e)
        }
    }
}
