/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.future;

import com.opengamma.masterdb.security.hibernate.EnumUserType;

/**
 * Hibernate bean for storage.
 */
public class FutureTypeUserType extends EnumUserType<FutureType> {

  private static final String AGRICULTURE_FUTURE_TYPE = "Agriculture";
  private static final String BOND_FUTURE_TYPE = "Bond";
  private static final String FX_FUTURE_TYPE = "FX";
  private static final String ENERGY_FUTURE_TYPE = "Energy";
  private static final String EQUITY = "Equity";
  private static final String EQUITY_INDEX_DIVIDEND = "Equity Index Dividend";
  private static final String INTEREST_RATE_FUTURE_TYPE = "Interest Rate";
  private static final String METAL_FUTURE_TYPE = "Metal";
  private static final String INDEX_FUTURE_TYPE = "Index";
  private static final String STOCK_FUTURE_TYPE = "Stock";

  public FutureTypeUserType() {
    super(FutureType.class, FutureType.values());
  }

  @Override
  protected String enumToStringNoCache(FutureType value) {
    return value.accept(new FutureType.Visitor<String>() {

      @Override
      public String visitBondFutureType() {
        return BOND_FUTURE_TYPE;
      }

      @Override
      public String visitFXFutureType() {
        return FX_FUTURE_TYPE;
      }

      @Override
      public String visitInterestRateFutureType() {
        return INTEREST_RATE_FUTURE_TYPE;
      }

      @Override
      public String visitAgricultureFutureType() {
        return AGRICULTURE_FUTURE_TYPE;
      }

      @Override
      public String visitEnergyFutureType() {
        return ENERGY_FUTURE_TYPE;
      }

      @Override
      public String visitMetalFutureType() {
        return METAL_FUTURE_TYPE;
      }

      @Override
      public String visitIndexFutureType() {
        return INDEX_FUTURE_TYPE;
      }

      @Override
      public String visitStockFutureType() {
        return STOCK_FUTURE_TYPE;
      }

      @Override
      public String visitEquityFutureType() {
        return EQUITY;
      }

      @Override
      public String visitEquityIndexDividendFutureType() {
        // TODO Auto-generated method stub
        return EQUITY_INDEX_DIVIDEND;
      }

    });
  }

}
