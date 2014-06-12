/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

// This is an example that computes present values under all bumps required to produce
// 'Bucketed PV01' (IR Delta) for the two curves specified in CurveExposures = USD_ON-OIS_LIBOR3M-FRAIRS
// of devsvr-lx-6, as of 2014-04-01. 
// Computation of the deltas themselves via FD approximation must be done as post-processing
view {
  name "zCase - Scenario Test"
  server "devsvr-lx-6:8080" 
  marketData {
    live "Bloomberg"
//    snapshot "Test View - Swaption SABR - 2014-03-18 - UnStructured" // Contains vol cube needed to price swpns in sabr
  }
}

// creates the cartesian product of the parameters and runs a scenario for each
// the alternative is shockList where there is a scenario "USing vol[0] and spot[0], vol[1] and spot[1], ...

shockGrid {
  ticker = [
    "USDR1T Curncy", // OIS Curve
    "USDR2T Curncy",
    "USSOA Curncy",
    "USSOB Curncy",
    "USSOC Curncy",
    "USSOF Curncy",
    "USSOI Curncy",
    "USSO1 Curncy",
    "USSO2 Curncy",
    "USSO3 Curncy",
    "USSO4 Curncy",
    "USSO5 Curncy",
    "USSO6 Curncy",
    "USSO7 Curncy",
    "USSO8 Curncy",
    "USSO9 Curncy",
    "USSO10 Curncy",
    "US0003M Index", // Vanilla Libor Swap Curve (USD is 3M)
    "USFR0CF Curncy",
    "USFR0FI Curncy",
    "USSW1 Curncy",
    "USSW2 Curncy",
    "USSW3 Curncy",
    "USSW4 Curncy",
    "USSW5 Curncy",
    "USSW7 Curncy",
    "USSW10 Curncy",
    "USSW12 Curncy",
    "USSW15 Curncy",
    "USSW20 Curncy",
    "USSW25 Curncy",
    "USSW30 Curncy"]
  slide = [-1.bp, 1.bp]
}

scenarios {
//  valuationTime "2014-04-02 12:00" // optional, defaults to now
  marketData {
    id "BLOOMBERG_TICKER", ticker
    apply {
      shift Absolute, slide
    }
  }
}