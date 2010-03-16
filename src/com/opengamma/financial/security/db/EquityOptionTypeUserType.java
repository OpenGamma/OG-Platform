/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;


/**
 * Custom Hibernate usertype for the EquityOptionType enum
 * 
 * @author andrew
 */
public class EquityOptionTypeUserType extends EnumUserType<EquityOptionType> {
  
  private static final String AMERICAN_EQUITY_OPTION_TYPE = "American";
  private static final String EUROPEAN_EQUITY_OPTION_TYPE = "European";
  private static final String POWERED_EQUITY_OPTION_TYPE = "Powered";

  public EquityOptionTypeUserType () {
    super (EquityOptionType.class, EquityOptionType.values ());
  }

  @Override
  protected String enumToStringNoCache(EquityOptionType value) {
    return value.accept (new EquityOptionType.Visitor<String> () {

      @Override
      public String visitAmericanEquityOptionType() {
        return AMERICAN_EQUITY_OPTION_TYPE;
      }

      @Override
      public String visitEuropeanEquityOptionType() {
        return EUROPEAN_EQUITY_OPTION_TYPE;
      }

      @Override
      public String visitPoweredEquityOptionType() {
        return POWERED_EQUITY_OPTION_TYPE;
      }
    });
  }
  
}