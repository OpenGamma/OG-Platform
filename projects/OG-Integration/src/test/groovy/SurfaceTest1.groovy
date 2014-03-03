/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

scenario 'relative surface test', {
  surface {
    apply {
      shifts Relative, {
        shift 6.m, 1.5, 10.pc
        shift 1.y, 2.5, 20.pc
      }
    }
  }
}
