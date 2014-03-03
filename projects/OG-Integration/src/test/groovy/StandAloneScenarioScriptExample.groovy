/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

view {
  name "an example view"
  server "devsvr-lx-2:8080" // optional, for windows installs can load from registry, for others from config file

  marketData {
    live "Bloomberg"
    snapshot "a snapshot name"
    fixedHistorical "2013-06-02"
    latestHistorical
  }
}

// creates the cartesian product of the parameters and runs a scenario for each
// the alternative is shockList where there is a scenario using vol[0] and spot[0], vol[1] and spot[1], ...
shockGrid {
  vol = [-10.pc, -5.pc, 0, 5.pc, 10.pc]
  spot = [-10.pc, -5.pc, 0, 5.pc, 10.pc]
}

scenarios {
  valuationTime "2014-01-28 12:00" // optional, defaults to now
  spotRate {
    currencyPair "USDMXN"
    apply {
      scaling spot
    }
  }
  marketData {
    //... use vol here
  }
}
