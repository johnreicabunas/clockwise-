package com.johnreicabunas.clockwise.data.repository

class BillingConnectionGate {
    private var isConnecting = false

    fun tryStartConnecting(): Boolean {
        if (isConnecting) {
            return false
        }
        isConnecting = true
        return true
    }

    fun onConnectionFinished() {
        isConnecting = false
    }
}
