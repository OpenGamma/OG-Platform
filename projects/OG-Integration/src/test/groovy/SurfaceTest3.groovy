/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
scenario 'surface index shifts', {
  surface {
    apply {
      indexShifts Absolute, 0, 1.bp, 2.bp
    }
  }
}
