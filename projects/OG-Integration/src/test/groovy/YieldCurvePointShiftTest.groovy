/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
scenario 'point shift test', {
  curve {
    apply {
      pointShifts Relative, {
        shift 2, 10.pc
        shift 3, 20.pc
      }
    }
  }
}
