/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.yield;

import java.io.Serializable;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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
   * The US real yield convention. Used for TIPS (see Federal Register Vol. 69, N0. 170, p 53623).
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
   * US bond (T-bill and treasuries) yield convention - US treasury for all periods but the last, in which case use money-market
   */
  public static final YieldConvention US_BOND = new SimpleYieldConvention("US Treasury"); //TODO better name
  /**
   * Bund (German government-issued bond) yield convention
   */
  public static final YieldConvention GERMAN_BOND = new SimpleYieldConvention("GERMAN BONDS");
  /**
   * US short duration T-bill.
   */
  public static final YieldConvention DISCOUNT = new SimpleYieldConvention("DISCOUNT");
  /**
   * Bill convention (in particular for Germany, United Kingdom, Belgium)
   */
  public static final YieldConvention INTERESTATMTY = new SimpleYieldConvention("INTEREST@MTY");
  /**
   * Some bonds have this code.
   */
  public static final YieldConvention STEP_FLOATER = new SimpleYieldConvention("STEP FLOATER"); // TODO: check if real yield convention

  /**
   * The convention name.
   */
  private final String _name;

  /**
   * Creates an instance.
   * @param name  the convention name, not null
   */
  protected SimpleYieldConvention(final String name) {
    Validate.notNull(name, "name");
    _name = name;
  }

  @Override
  public String getConventionName() {
    return _name;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
  //REVIEW emcleod 28-1-2011 Is the lack of hashCode() and equals() deliberate?
}
