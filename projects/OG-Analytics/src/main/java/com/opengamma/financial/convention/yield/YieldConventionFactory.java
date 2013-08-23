/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.yield;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.joda.convert.FromString;

import com.opengamma.util.ArgumentChecker;

/**
 * Factory to obtain instances of {@code YieldConvention}.
 */
public final class YieldConventionFactory {

  /**
   * Singleton instance.
   */
  public static final YieldConventionFactory INSTANCE = new YieldConventionFactory();

  /**
   * Map of convention name to convention.
   */
  private final Map<String, YieldConvention> _conventionMap = new HashMap<>();

  //-------------------------------------------------------------------------
  /**
   * Gets a convention by name.
   * Matching is case insensitive.
   *
   * @param name  the name, not null
   * @return the convention, not null
   * @throws IllegalArgumentException if not found
   */
  @FromString
  public static YieldConvention of(final String name) {
    final YieldConvention result = YieldConventionFactory.INSTANCE.getYieldConvention(name);
    if (result == null) {
      throw new IllegalArgumentException("Unknown YieldConvention: " + name);
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the factory.
   */
  private YieldConventionFactory() {
    store(SimpleYieldConvention.UK_BUMP_DMO_METHOD, "UK:BUMP/DMO METHOD");
    store(SimpleYieldConvention.UK_STRIP_METHOD, "UK STRIP METHOD");
    store(SimpleYieldConvention.US_STREET);
    store(SimpleYieldConvention.US_IL_REAL);
    store(SimpleYieldConvention.US_IL_REAL);
    store(SimpleYieldConvention.US_IL_REAL, "U.S. I/L REAL YLD");
    store(SimpleYieldConvention.US_STREET, "STREET CONVENTION");
    store(SimpleYieldConvention.US_TREASURY_EQUIVALANT);
    store(SimpleYieldConvention.JGB_SIMPLE);
    store(SimpleYieldConvention.MONEY_MARKET);
    store(SimpleYieldConvention.TRUE);
    store(SimpleYieldConvention.US_BOND);
    store(SimpleYieldConvention.GERMAN_BOND);
    store(SimpleYieldConvention.DISCOUNT);
    store(SimpleYieldConvention.INTERESTATMTY);
    store(SimpleYieldConvention.STEP_FLOATER);
    store(SimpleYieldConvention.JAPAN_SIMPLE);
    store(SimpleYieldConvention.BANK_OF_CANADA, "BANK OF CANADA YLD");
    store(SimpleYieldConvention.CANADA_COMPND_METHOD, "CANADA:COMPND METH");
    store(SimpleYieldConvention.PAY_IN_KIND);
    store(SimpleYieldConvention.FLOAT_RATE_NOTE);
    store(SimpleYieldConvention.TOGGLE_PIK_NOTES);
    store(SimpleYieldConvention.INTEREST_AT_MATURITY);
    store(SimpleYieldConvention.FRANCE_COMPOUND_METHOD);
    store(SimpleYieldConvention.SPAIN_GOVERNMENT_BONDS);
    store(SimpleYieldConvention.GREEK_GOVERNMENT_BONDS);
    store(SimpleYieldConvention.FINLAND_GOVERNMENT_BONDS);
    store(SimpleYieldConvention.AUSTRIA_ISMA_METHOD);
    store(SimpleYieldConvention.ITALY_TREASURY_BONDS);
    store(SimpleYieldConvention.SPANISH_T_BILLS);
    store(SimpleYieldConvention.PORTUGAL_DOMESTIC_SETTLE);
    store(SimpleYieldConvention.ITALY_TREASURY_BILL);
  }

  /**
   * Stores the convention.
   *
   * @param convention  the convention to store, not null
   */
  private void store(final YieldConvention convention) {
    ArgumentChecker.notNull(convention, "YieldConvention");
    _conventionMap.put(convention.getConventionName().toLowerCase(Locale.ENGLISH), convention);
  }

  /**
   * Stores the convention with an alternative string name.
   *
   * @param convention  the convention to store, not null
   * @param name the alternative name for the convention, not null
   */
  private void store(final YieldConvention convention, final String name) {
    ArgumentChecker.notNull(convention, "YieldConvention");
    _conventionMap.put(name.toLowerCase(Locale.ENGLISH), convention);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a convention by name.
   * Matching is case insensitive.
   *
   * @param name  the name, not null
   * @return the convention, null if not found
   */
  public YieldConvention getYieldConvention(final String name) {
    ArgumentChecker.notNull(name, "name");
    return _conventionMap.get(name.toLowerCase(Locale.ENGLISH));
  }

}
