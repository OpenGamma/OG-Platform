/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

simulation 'sim', {
  scenario 's1', {
    curve {
      apply {
        parallelShift 1.bp
      }
    }
  }
  scenario 's2', {
    curve {
      apply {
        parallelShift 1.pc
      }
    }
  }
  scenario 's3', {
    curve {
      apply {
        parallelShift 5.percent
      }
    }
  }
}
