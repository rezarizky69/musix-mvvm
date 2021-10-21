package com.eja.musix.other

// class untuk mewakili trigger di aplikasi melalui event dalam bentuk generic
// serta mempunyai parameter dari data yg juga bersifat generic
open class Event<out T>(private val data: T) {

    // variabel pengecekan apakah perlu di handle
    // dengan nilai awal false
    var hasBeenHandled = false
        private set

    // function untuk mengambil konten apabila belum di handle
    // return type generic
    fun getContentIfNotHandled(): T? {
        // jika sudah di handle
        return if (hasBeenHandled) {
            // maka tidak perlu mengembalikan apa"
            null
            // jika belum
        } else {
            // maka set apakah perlu di handle dengan true
            hasBeenHandled = true
            // dan isi dengan data
            data
        }
    }

    // tidak digunakan, hanya sebagai formalitas dari google implementation
    fun peekContent() = data
}