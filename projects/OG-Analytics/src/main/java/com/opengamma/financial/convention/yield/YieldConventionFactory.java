/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.yield;

import java.util.Locale;

import org.joda.convert.FromString;

import com.opengamma.financial.convention.AbstractNamedInstanceFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Factory to obtain instances of {@code YieldConvention}.
 */
public final class YieldConventionFactory
    extends AbstractNamedInstanceFactory<YieldConvention> {

  /**
   * Singleton instance.
   */
  public static final YieldConventionFactory INSTANCE = new YieldConventionFactory();

  //-------------------------------------------------------------------------
  /**
   * Finds a convention by name, ignoring case.
   *
   * @param name  the name of the instance to find, not null
   * @return the convention, not null
   * @throws IllegalArgumentException if the name is not found
   */
  @FromString
  public static YieldConvention of(final String name) {
    try {
      return INSTANCE.instance(name);
    } catch (final IllegalArgumentException ex) {
      ArgumentChecker.notNull(name, "name");
      final YieldConvention yc = new SimpleYieldConvention(name.toLowerCase(Locale.ENGLISH));
      return INSTANCE.addInstance(yc);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor, hard coding the conventions.
   */
  private YieldConventionFactory() {
    super(YieldConvention.class);
    addInstance(SimpleYieldConvention.UK_BUMP_DMO_METHOD, "UK:BUMP/DMO METHOD");
    addInstance(SimpleYieldConvention.UK_STRIP_METHOD, "UK STRIP METHOD");
    addInstance(SimpleYieldConvention.US_STREET);
    addInstance(SimpleYieldConvention.US_IL_REAL);
    addInstance(SimpleYieldConvention.US_IL_REAL);
    addInstance(SimpleYieldConvention.US_IL_REAL, "U.S. I/L REAL YLD");
    addInstance(SimpleYieldConvention.US_STREET, "STREET CONVENTION");
    addInstance(SimpleYieldConvention.US_TREASURY_EQUIVALENT);
    addInstance(SimpleYieldConvention.JGB_SIMPLE);
    addInstance(SimpleYieldConvention.MONEY_MARKET);
    addInstance(SimpleYieldConvention.TRUE);
    addInstance(SimpleYieldConvention.US_BOND);
    addInstance(SimpleYieldConvention.GERMAN_BOND);
    addInstance(SimpleYieldConvention.DISCOUNT);
    addInstance(SimpleYieldConvention.INTERESTATMTY);
    addInstance(SimpleYieldConvention.STEP_FLOATER);
    addInstance(SimpleYieldConvention.JAPAN_SIMPLE);
    addInstance(SimpleYieldConvention.BANK_OF_CANADA, "BANK OF CANADA YLD");
    addInstance(SimpleYieldConvention.CANADA_COMPND_METHOD, "CANADA:COMPND METH");
    addInstance(SimpleYieldConvention.PAY_IN_KIND);
    addInstance(SimpleYieldConvention.FLOAT_RATE_NOTE);
    addInstance(SimpleYieldConvention.TOGGLE_PIK_NOTES);
    addInstance(SimpleYieldConvention.INTEREST_AT_MATURITY);
    addInstance(SimpleYieldConvention.FRANCE_COMPOUND_METHOD);
    addInstance(SimpleYieldConvention.SPAIN_GOVERNMENT_BONDS);
    addInstance(SimpleYieldConvention.GREEK_GOVERNMENT_BONDS);
    addInstance(SimpleYieldConvention.FINLAND_GOVERNMENT_BONDS);
    addInstance(SimpleYieldConvention.AUSTRIA_ISMA_METHOD);
    addInstance(SimpleYieldConvention.ITALY_TREASURY_BONDS);
    addInstance(SimpleYieldConvention.SPANISH_T_BILLS);
    addInstance(SimpleYieldConvention.PORTUGAL_DOMESTIC_SETTLE);
    addInstance(SimpleYieldConvention.ITALY_TREASURY_BILL);
    addInstance(SimpleYieldConvention.MEXICAN_BONOS);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a convention by name.
   * Matching is case insensitive.
   * <p>
   * This method dynamically creates the convention if it is missing.
   *
   * @param name  the name, not null
   * @return the convention, null if not found
   */
  public YieldConvention getYieldConvention(final String name) {
    try {
      return instance(name);
    } catch (final IllegalArgumentException ex) {
      ArgumentChecker.notNull(name, "name");
      final YieldConvention yc = new SimpleYieldConvention(name.toUpperCase(Locale.ENGLISH));
      return addInstance(yc);
    }
  }

}
