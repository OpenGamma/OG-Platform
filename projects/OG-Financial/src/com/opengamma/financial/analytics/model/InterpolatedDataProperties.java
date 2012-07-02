/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

/**
 * 
 */
public class InterpolatedDataProperties {
  /** String representing the calculation method  */
  public static final String CALCULATION_METHOD_NAME = "Interpolated";
  /** String labelling the x extrapolator (left and right) */
  public static final String X_EXTRAPOLATOR_NAME = "XExtrapolator";
  /** String labelling the left x extrapolator */
  public static final String LEFT_X_EXTRAPOLATOR_NAME = "LeftXExtrapolator";
  /** String labelling the right x extrapolator */
  public static final String RIGHT_X_EXTRAPOLATOR_NAME = "RightXExtrapolator";
  /** String labelling the x interpolator */
  public static final String X_INTERPOLATOR_NAME = "XInterpolator";
  /** String labelling the y extrapolator (left and right) */
  public static final String Y_EXTRAPOLATOR_NAME = "YExtrapolator";
  /** String labelling the left y extrapolator */
  public static final String LEFT_Y_EXTRAPOLATOR_NAME = "LeftYExtrapolator";
  /** String labelling the right y extrapolator */
  public static final String RIGHT_Y_EXTRAPOLATOR_NAME = "RightYExtrapolator";
  /** String labelling the y interpolator */
  public static final String Y_INTERPOLATOR_NAME = "YInterpolator";
}
