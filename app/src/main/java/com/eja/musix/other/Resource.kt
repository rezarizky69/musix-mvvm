package com.eja.musix.other

// class resource sbg source yg mewakili status dan membawa data berupa generic dan
// message error berupa string
data class Resource<out T>(val status: Status, val data: T?, val message: String?) {

    companion object {
        // jika status sukses, maka return dengan status sukses, berisi data dan tidak membawa
        // pesan error
        fun <T> success(data: T?) = Resource(Status.SUCCESS, data, null)

        // jika status error, maka return dengan status error, berisi data dan  membawa
        // pesan error
        fun <T> error(message: String, data: T?) = Resource(Status.ERROR, data, message)

        // jika status loading, maka return dengan status loading, berisi data dan tidak membawa
        // pesan error
        fun <T> loading(data: T?) = Resource(Status.LOADING, data, null)
    }
}

// inisialisasi constant untuk per state status
enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}