/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model;

/**
 * Simple enum to indicate whether a bump (shift) is additive (i.e. newValue = oldValue + bump) or multiplicative  
 * (i.e. newValue = oldValue * (1 + bump)  = oldValue + oldValue * bump)
 */
public enum BumpType {
  /**
   * The bump (shift) is newValue = oldValue + bump
   */
  ADDITIVE,
  /**
   * The bump (shift) is newValue = oldValue * (1 + bump)
   */
  MULTIPLICATIVE
}
