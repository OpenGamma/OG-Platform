/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.generator;

import java.math.BigDecimal;

import com.opengamma.financial.generator.MixedFXPortfolioGeneratorTool;
import com.opengamma.financial.generator.SecurityPersister;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;

/**
 * Example portfolio generator that overrides {@link MixedFXPortfolioGeneratorTool} to only generate EURUSD
 * trades and to not generate any forwards.
 */
public class EuroDollarFXPortfolioGeneratorTool extends MixedFXPortfolioGeneratorTool {

  private static Currency[] s_currencies = {Currency.EUR, Currency.USD};

  @Override
  protected <T extends ManageableSecurity> MixedFXSecurityGenerator<T> createMixedFXSecurityGenerator() {
    return new FXSecurityGenerator<T>();
  }

  private class FXSecurityGenerator<T extends ManageableSecurity> extends MixedFXSecurityGenerator<T> {

    @Override
    public Currency[] getCurrencies() {
      return s_currencies;
    }

    /**
     * We don't want FX forwards in the generated portfolio so this always returns null.
     * @return {@code null}
     */
    @Override
    protected ManageableTrade createFXForwardSecurityTrade(Bundle bundle, BigDecimal quantity, SecurityPersister persister) {
      return null;
    }
  }
}
