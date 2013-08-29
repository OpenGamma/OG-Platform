/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabr;

/**
 *
 */
public class SABRPropertyValues {

  /**
   * Property name for the no (right) extrapolation calculation method.
   */
  public static final String NO_EXTRAPOLATION = "NoExtrapolation";
  /**
   * Property name for the right extrapolation calculation method.
   */
  public static final String RIGHT_EXTRAPOLATION = "RightExtrapolation";
  /**
   * Property name for the strike cutoff to be used with the right extrapolation method.
   */
  public static final String PROPERTY_STRIKE_CUTOFF = "StrikeCutoff";
  /**
   * Property name for the value of mu to be used with the right extrapolation method.
   */
  public static final String PROPERTY_MU = "Mu";
}
