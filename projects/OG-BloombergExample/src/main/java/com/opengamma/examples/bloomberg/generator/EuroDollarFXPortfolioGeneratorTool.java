/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.generator;

import java.math.BigDecimal;

import com.opengamma.financial.generator.MixedFXPortfolioGeneratorTool;
import com.opengamma.financial.generator.NameGenerator;
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

  @Override
  public Currency[] getCurrencies() {
    return s_currencies;
  }

  private class FXSecurityGenerator<T extends ManageableSecurity> extends MixedFXSecurityGenerator<T> {

    /**
     * We don't want FX forwards in the generated portfolio so this always returns null.
     * @return null
     */
    @Override
    protected ManageableTrade createFXForwardSecurityTrade(final Bundle bundle, final BigDecimal quantity, final SecurityPersister persister, final NameGenerator counterPartyGenerator) {
      return null;
    }
    
    /**
     * We don't want FX barriers in the generated portfolio as the generated trades don't always price, so this always returns null.
     * @return null
     */
    @Override
    protected ManageableTrade createFXBarrierOptionSecurityTrade(final Bundle bundle, final BigDecimal quantity, final SecurityPersister persister, final NameGenerator counterPartyGenerator) {
      return null;
    }
    
  }
}
