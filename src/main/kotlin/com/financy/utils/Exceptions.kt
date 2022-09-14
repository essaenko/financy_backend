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
  UnsupportedImportFileFormat,
  UnsupportedFileEncoding,

  //Category
  UnresolvedCategoryAccountException,
  UnresolvedCategoryException,

  //Payment
  UnresolvedPaymentMethodIdException,
  PaymentMethodNotFoundException,

  //PaymentAccount
  PaymentAccountNotFoundException,
  UnresolvedPaymentAccountIdException
}