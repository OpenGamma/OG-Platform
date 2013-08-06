/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

/**
 * Enum describing the interpolation method to be used for swap quotes (e.g. linear for inflation swap legs).
 */
public enum InterpolationMethod {

  //TODO is there any reason why these shouldn't be used in the swap conventions as well?
  /** Linear interpolation */
  MONTH_START_LINEAR,
  /** No interpolation */
  NONE
}
