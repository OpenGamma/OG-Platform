/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

/**
 * Enumeration of the user supplied data masters.
 */
public enum UserDataType {

  /**
   * Value from an InterpolatedYieldCurveDefinitionMaster
   */
  INTERPOLATED_YIELD_CURVE_DEFINITION,

  /**
   * Value from a SecurityMaster
   */
  SECURITY,

  /**
   * Value from a PositionMaster
   */
  POSITION,

  /**
   * Value from a PositionMaster
   */
  PORTFOLIO_TREE,

  /**
   * Value from a ManageableViewDefinitionRepository
   */
  VIEW_DEFINITION

}
