/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

parameters {
  foo String
  bar Number
}

scenario 'scenarioName', {
  marketData {
    id 'SCHEME', foo
    apply {
      scaling bar
    }
  }
}
