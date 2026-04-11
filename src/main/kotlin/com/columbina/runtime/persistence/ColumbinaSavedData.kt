package com.columbina.runtime.persistence

object ColumbinaSavedData {
    private var bootstrapped = false

    fun bootstrap() {
        if (bootstrapped) {
            return
        }

        bootstrapped = true
    }
}
