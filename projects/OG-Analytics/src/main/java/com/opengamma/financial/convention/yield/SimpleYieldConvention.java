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
   * Japanese bonds
   */
  public static final YieldConvention JAPAN_SIMPLE = new SimpleYieldConvention("JAPAN:SIMPLE YIELD");
  /**
   * Bank of Canada 
   */
  public static final YieldConvention BANK_OF_CANADA = new SimpleYieldConvention("BANK OF CANADA YLD");
  /**
   * Canada Compound Method
   */
  public static final YieldConvention CANADA_COMPND_METHOD = new SimpleYieldConvention("CANADA:COMPND METH");  
  /**
   * Pay-in-kind
   */
  public static final YieldConvention PAY_IN_KIND = new SimpleYieldConvention("PAY-IN-KIND");
  /**
   * Floating rate note
   */
  public static final YieldConvention FLOAT_RATE_NOTE = new SimpleYieldConvention("FLOAT RATE NOTE");
  /**
   * Toggle PIK Notes
   */
  public static final YieldConvention TOGGLE_PIK_NOTES = new SimpleYieldConvention("TOGGLE PIK NOTES");
  /**
   * Interest at Maturity
   */
  public static final YieldConvention INTEREST_AT_MATURITY = new SimpleYieldConvention("INTEREST AT MTY");
  /**
   * France Compound Method
   */
  public static final YieldConvention FRANCE_COMPOUND_METHOD = new SimpleYieldConvention("FRANCE:COMPND METH");
  /**
   * Spain Government Bonds
   */
  public static final YieldConvention SPAIN_GOVERNMENT_BONDS = new SimpleYieldConvention("SPAIN:GOVT BONDS");
  /**
   * Greek Government Bonds
   */
  public static final YieldConvention GREEK_GOVERNMENT_BONDS = new SimpleYieldConvention("GREEK GOVT BNDS");
  /**
   * Finland Government Bonds
   */
  public static final YieldConvention FINLAND_GOVERNMENT_BONDS = new SimpleYieldConvention("FINLAND GOVT BONDS");
  /**
   * Austria ISMA Method
   */
  public static final YieldConvention AUSTRIA_ISMA_METHOD = new SimpleYieldConvention("AUSTRIA:ISMA METHD");
  /**
   * Italy Treasury Bonds
   */
  public static final YieldConvention ITALY_TREASURY_BONDS = new SimpleYieldConvention("ITALY:TRSY BONDS");
  /** 
   * Spainish T-bills
   */
  public static final YieldConvention SPANISH_T_BILLS = new SimpleYieldConvention("SPANISH T-BILLS");
  /**
   * Portugal Domestic Settlement
   */
  public static final YieldConvention PORTUGAL_DOMESTIC_SETTLE = new SimpleYieldConvention("PORTUGAL-DOM. SETL");
  /**
   * Italy Treasury Bill
   */
  public static final YieldConvention ITALY_TREASURY_BILL = new SimpleYieldConvention("ITALY:TRSY BILL");
  
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
