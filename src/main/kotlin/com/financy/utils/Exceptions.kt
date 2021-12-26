package com.financy.utils

enum class Exceptions {
  NoUserAccountException,
  AccountNotFoundException,
  UserNotFoundException,
  InvalidRegistrationCredentialsException,
  InvalidUserCredentialsException,

  UnauthorizedOperationException,
  InternalServerException,

  UnresolvedTransactionCategoryException,
  UnresolvedTransactionIdException,
  UnresolvedTransactionException,
  UnresolvedTransactionPaymentMethodException,


  UnresolvedCategoryAccountException,

  UnresolvedPaymentMethodIdException,
  PaymentMethodNotFoundException
}