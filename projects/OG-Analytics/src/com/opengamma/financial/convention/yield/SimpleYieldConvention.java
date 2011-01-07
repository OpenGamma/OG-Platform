/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.yield;

import java.io.Serializable;

import com.opengamma.util.ArgumentChecker;

/**
 * A simple yield convention.
 */
public class SimpleYieldConvention implements YieldConvention, Serializable {
  // TODO: should be an enum?

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public static final YieldConvention UK_STRIP_METHOD = new SimpleYieldConvention("UK STRIP METHOD");
  /**
   * ?? this is the convention used by UK GILT STOCKS
   */
  public static final YieldConvention UK_BUMP_DMO_METHOD = new SimpleYieldConvention("UK:BUMP/DMO METHOD");
  /**
   * Dunno what this represents.
   * 
   */
  public static final YieldConvention US_IL_REAL = new SimpleYieldConvention("US I/L real");
  /**
   * The US street yield convention.
   */
  public static final YieldConvention US_STREET = new SimpleYieldConvention("US street");
  /**
   * The US treasury equivalent yield convention. 
   */
  public static final YieldConvention US_TREASURY_EQUIVALANT = new SimpleYieldConvention("US Treasury equivalent");
  /**
   * The money market yield convention.
   */
  public static final YieldConvention MONEY_MARKET = new SimpleYieldConvention("Money Market");
  /**
   * The JGB simple yield convention.
   */
  public static final YieldConvention JGB_SIMPLE = new SimpleYieldConvention("JGB simple");
  /**
   * The true yield convention.
   */
  public static final YieldConvention TRUE = new SimpleYieldConvention("True");

  /**
   * The convention name.
   */
  private final String _name;

  /**
   * Creates an instance.
   * @param name  the convention name, not null
   */
  protected SimpleYieldConvention(final String name) {
    ArgumentChecker.notNull(name, "name");
    _name = name;
  }

  // -------------------------------------------------------------------------
  @Override
  public String getConventionName() {
    return _name;
  }

}
