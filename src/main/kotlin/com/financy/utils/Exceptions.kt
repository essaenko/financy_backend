package com.financy.utils

enum class Exceptions {
  //User
  UserNotFoundException,
  InvalidRegistrationCredentialsException,
  InvalidUserCredentialsException,
  EmailAlreadyRegisteredException,

  //Account
  NoUserAccountException,
  AccountNotFoundException,

  //Permissions
  InvalidTokenException,
  NotPermittedOperation,

  //Serverside
  InternalServerException,
  BadRequestException,

  //Transaction
  UnresolvedTransactionCategoryException,
  UnresolvedTransactionIdException,
  UnresolvedTransactionException,
  UnresolvedTransactionPaymentMethodException,

  //Category
  UnresolvedCategoryAccountException,

  //Payment
  UnresolvedPaymentMethodIdException,
  PaymentMethodNotFoundException
}