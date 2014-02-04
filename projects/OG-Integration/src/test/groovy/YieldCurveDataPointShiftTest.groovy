/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
scenario 'point shift test', {
  curveData {
    apply {
      pointShifts Relative, {
        shift 3.m, 10.pc
        shift 1.y, 20.pc
      }
    }
  }
}
