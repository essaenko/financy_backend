package com.financy.utils

enum class Exceptions {
  NotPermittedOperation,
  NoUserAccountException,
  AccountNotFoundException,
  UserNotFoundException,
  InvalidRegistrationCredentialsException,
  InvalidUserCredentialsException,

  UnauthorizedOperationException,
  InternalServerException,
  BadRequestException,

  UnresolvedTransactionCategoryException,
  UnresolvedTransactionIdException,
  UnresolvedTransactionException,
  UnresolvedTransactionPaymentMethodException,

  UnresolvedCategoryAccountException,

  UnresolvedPaymentMethodIdException,
  PaymentMethodNotFoundException
}