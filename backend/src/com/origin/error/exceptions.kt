package com.origin.error

class UserNotFound : RuntimeException()
class WrongPassword : RuntimeException()
class UserAlreadyExists : RuntimeException()
class AuthorizationException(msg: String? = null) : RuntimeException(msg)
class BadRequest(msg: String) : RuntimeException(msg)