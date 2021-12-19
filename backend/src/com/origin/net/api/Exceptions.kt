package com.origin.net.api

class UserNotFound : RuntimeException()
class WrongPassword : RuntimeException()
class UserExists : RuntimeException()
class AuthorizationException(msg: String? = null) : RuntimeException(msg)
class BadRequest(msg: String) : RuntimeException(msg)
