package smartweb.controller

class ReferenceValue<T>(value: T?, private val listener: (T?) -> Unit) {

    var value: T? = value
        set(value) {
            field = value
            listener.invoke(value)
        }

}