/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

scenario 'spot rate test', {
  spotRate {
    currencyPair 'EURUSD'
    apply {
      shift 0.1
    }
  }
  spotRate {
    currencyPair 'GBPAUD'
    apply {
      scaling 20.pc
    }
  }
  spotRate {
    currencyPair 'EURCAD'
    apply {
      replace 1.5
    }
  }
}
