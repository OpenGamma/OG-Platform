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
public class OptionSecurityTypeUserType extends EnumUserType<OptionSecurityType> {
  
  private static final String AMERICAN_EQUITY_OPTION_TYPE = "American equity";
  private static final String AMERICAN_FUTURE_OPTION_TYPE = "American future";
  private static final String EUROPEAN_EQUITY_OPTION_TYPE = "European equity";
  private static final String EUROPEAN_FUTURE_OPTION_TYPE = "European future";
  private static final String FX_OPTION_TYPE = "FX";
  private static final String POWERED_EQUITY_OPTION_TYPE = "Powered equity";

  public OptionSecurityTypeUserType() {
    super(OptionSecurityType.class, OptionSecurityType.values());
  }

  @Override
  protected String enumToStringNoCache(OptionSecurityType value) {
    return value.accept(new OptionSecurityType.Visitor<String>() {

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

      @Override
      public String visitAmericanFutureOptionType() {
        return AMERICAN_FUTURE_OPTION_TYPE;
      }

      @Override
      public String visitEuropeanFutureOptionType() {
        return EUROPEAN_FUTURE_OPTION_TYPE;
      }

      @Override
      public String visitFXOptionType() {
        return FX_OPTION_TYPE;
      }
    });
  }
  
}