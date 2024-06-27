package com.origin.error

class UserNotFoundException : RuntimeException()
class WrongPasswordException : RuntimeException()
class UserAlreadyExistsException : RuntimeException()
class AuthorizationException(msg: String? = null) : RuntimeException(msg)
class BadRequestException(msg: String) : RuntimeException(msg)
