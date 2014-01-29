/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

scenario 'absolute surface test', {
  surface {
    apply {
      shifts Absolute, {
        shift 0.5, 0.6, 0.1
        shift 1.5, 0.7, 0.2
      }
    }
  }
}
