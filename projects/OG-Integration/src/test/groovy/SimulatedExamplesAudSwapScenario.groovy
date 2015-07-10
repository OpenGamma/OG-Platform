/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

view {
  name 'AUD Swaps (3m / 6m basis) (1)'
  server 'localhost:8080'
  marketData {
    live 'Simulated live market data'
  }
}

shockList {
  curveShock = [-10.pc, -5.pc, 0, 5.pc, 10.pc]
  fooShock = [-0.5, -0.2, 0, 0.2, 0.5]
}

scenarios {

  curveData {
    apply {
      bucketedShifts Relative, {
        shift 0.y, 1.y, curveShock
      }
    }
  }

  marketData {
    id "FOO", "123"
    apply {
      shift Absolute, fooShock
    }
  }
}
