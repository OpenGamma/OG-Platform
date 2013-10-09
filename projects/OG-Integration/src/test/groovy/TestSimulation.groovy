/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

simulation {
  shifts = [-0.0025d, -0.001d, 0, 0.001d, 0.0025d]

  for (shift in shifts) {

    name = "${(int) shift * 10000}bps"

    scenario name, {
      curve {
        named 'Discount', 'Forward6M'
        currencies 'EUR'
        apply {
          parallelShift shift
        }
      }
    }
  }
}
