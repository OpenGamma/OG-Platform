/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

view {
  name "an example view"
  server "devsvr-lx-2:8080" // optional, for windows installs can load from registry, for others from config file
  valuationTime "2014-01-28 12:00" // optional, defaults to now
  aggregation "Currency", "Asset Class" // optional

  marketData {
    live "Bloomberg"
    snapshot "a snapshot name"
    fixedHistorical "2013-06-02"
    latestHistorical
  }
}

shockGrid {
  vol = [-10.pc, -5.pc, 0, 5.pc, 10.pc]
  spot = [-10.pc, -5.pc, 0, 5.pc, 10.pc]
}

// how do I express how to combine the values? cartesian product or zip?
scenarios {
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
