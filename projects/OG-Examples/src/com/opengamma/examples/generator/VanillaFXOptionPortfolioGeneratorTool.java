/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;

import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.LeafPortfolioNodeGenerator;
import com.opengamma.financial.generator.NameGenerator;
import com.opengamma.financial.generator.PortfolioGenerator;
import com.opengamma.financial.generator.PortfolioNodeGenerator;
import com.opengamma.financial.generator.PositionGenerator;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.generator.SimplePositionGenerator;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class VanillaFXOptionPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {
  private static final FXOptionSecurity[] FX_OPTION = new FXOptionSecurity[4];

  static {
    final ExerciseType european = new EuropeanExerciseType();
    final FXOptionSecurity fxOption1 = new FXOptionSecurity(Currency.USD, Currency.EUR, 1, 0.8, new Expiry(DateUtils.getUTCDate(2014, 1, 13)), DateUtils.getUTCDate(2014, 1, 13),
        true, european);
    fxOption1.setName("Long 1.25 USD/EUR expiring 2014-1-13");
    FX_OPTION[0] = fxOption1;
    final FXOptionSecurity fxOption2 = new FXOptionSecurity(Currency.EUR, Currency.USD, 1, 1, new Expiry(DateUtils.getUTCDate(2020, 1, 13)), DateUtils.getUTCDate(2020, 1, 13),
        true, european);
    fxOption2.setName("Long 1 EUR/USD expiring 2020-1-13");
    FX_OPTION[1] = fxOption2;
    final FXOptionSecurity fxOption3 = new FXOptionSecurity(Currency.USD, Currency.EUR, 1, 1.5, new Expiry(DateUtils.getUTCDate(2016, 1, 13)), DateUtils.getUTCDate(2016, 1, 13),
        true, european);
    fxOption3.setName("Long 1.5 EUR/USD expiring 2016-1-13");
    FX_OPTION[2] = fxOption3;
    final FXOptionSecurity fxOption4 = new FXOptionSecurity(Currency.EUR, Currency.USD, 1, 2, new Expiry(DateUtils.getUTCDate(2019, 1, 13)), DateUtils.getUTCDate(2019, 1, 13),
        true, european);
    fxOption4.setName("Long 2 USD/EUR  expiring 2014-1-13");
    FX_OPTION[3] = fxOption4;
  }

  @Override
  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final SecurityGenerator<FXOptionSecurity> securities = createFXOptionSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<FXOptionSecurity>(securities, getSecurityPersister(), getCounterPartyGenerator());
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("EUR / USD FX Option"), positions, 4);
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int portfolioSize) {
    final SecurityGenerator<FXOptionSecurity> securities = createFXOptionSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<FXOptionSecurity>(securities, getSecurityPersister(), getCounterPartyGenerator());
    return new LeafPortfolioNodeGenerator(new StaticNameGenerator("EUR / USD FX Option"), positions, 4);
  }

  private SecurityGenerator<FXOptionSecurity> createFXOptionSecurityGenerator() {
    final SecurityGenerator<FXOptionSecurity> securities = new SecurityGenerator<FXOptionSecurity>() {
      private int _count;

      @Override
      public FXOptionSecurity createSecurity() {
        if (_count > 3) {
          throw new IllegalStateException("Should not ask for more than 4 securities");
        }
        final FXOptionSecurity fxOption = FX_OPTION[_count++];
        return fxOption;
      }

    };
    configure(securities);
    return securities;
  }
}
