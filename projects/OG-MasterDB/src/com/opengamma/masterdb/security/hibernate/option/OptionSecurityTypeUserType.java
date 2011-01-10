/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import com.opengamma.financial.security.option.BondOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FutureOptionSecurity;
import com.opengamma.financial.security.option.OptionOptionSecurity;
import com.opengamma.financial.security.option.OptionSecurityVisitor;
import com.opengamma.financial.security.option.SwapOptionSecurity;
import com.opengamma.masterdb.security.hibernate.EnumUserType;

/**
 * Custom Hibernate usertype for the EquityOptionType enum
 */
public class OptionSecurityTypeUserType extends EnumUserType<OptionSecurityType> {
  
  private static final String BOND = "Bond";
  private static final String EQUITY = "Equity";
  private static final String FX = "FX";
  private static final String FUTURE = "Future";
  private static final String OPTION = "Option";
  private static final String SWAP = "Swap";

  public OptionSecurityTypeUserType() {
    super(OptionSecurityType.class, OptionSecurityType.values());
  }

  @Override
  protected String enumToStringNoCache(OptionSecurityType value) {
    return value.accept(new OptionSecurityVisitor<String>() {

      @Override
      public String visitBondOptionSecurity(BondOptionSecurity security) {
        return BOND;
      }

      @Override
      public String visitEquityOptionSecurity(EquityOptionSecurity security) {
        return EQUITY;
      }

      @Override
      public String visitFXOptionSecurity(FXOptionSecurity security) {
        return FX;
      }

      @Override
      public String visitFutureOptionSecurity(FutureOptionSecurity security) {
        return FUTURE;
      }

      @Override
      public String visitOptionOptionSecurity(OptionOptionSecurity security) {
        return OPTION;
      }

      @Override
      public String visitSwapOptionSecurity(SwapOptionSecurity security) {
        return SWAP;
      }
    });
  }
  
}
