/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

scenario 'spot rate test', {
  spotRate {
    currencyPair 'EURUSD'
    apply {
      shift Absolute, 0.1
    }
  }
  spotRate {
    currencyPair 'GBPAUD'
    apply {
      scaling 1.2
    }
  }
  spotRate {
    currencyPair 'EURCAD'
    apply {
      replace 1.5
    }
  }
}
