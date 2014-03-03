/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
scenario 'bucketed shift test', {
  curve {
    apply {
      bucketedShifts Absolute, {
        shift 3.m, 6.m, 10.bp
        shift 1.y, 2.y, 20.bp
      }
    }
  }
}
