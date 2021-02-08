package com.origin.net.api

class UserNotFound : RuntimeException()
class WrongPassword : RuntimeException()
class UserExists : RuntimeException()
class AuthorizationException : RuntimeException()
class BadRequest(msg: String) : RuntimeException(msg)