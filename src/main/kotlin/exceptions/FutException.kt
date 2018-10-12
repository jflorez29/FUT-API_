package exceptions

class FutException(override var message : String) : Exception()

class ExpiredSessionException(override var message : String) : Exception()