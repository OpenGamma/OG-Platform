/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.generator;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.id.ExternalId;
import com.opengamma.util.function.BiFunction;
import com.opengamma.util.money.Currency;

/**
 * Portfolio generator for Bloomberg exmaples.
 */
public class BloombergExamplePortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {

  @Override
  protected void configureChain(final SecurityGenerator<?> securityGenerator) {
    super.configureChain(securityGenerator);
    securityGenerator.setCurrencyCurveName("Discounting");
    securityGenerator.setCurveCalculationConfig(Currency.CHF, "DefaultTwoCurveCHFConfig");
    securityGenerator.setCurveCalculationConfig(Currency.EUR, "DefaultTwoCurveEURConfig");
    securityGenerator.setCurveCalculationConfig(Currency.GBP, "DefaultTwoCurveGBPConfig");
    securityGenerator.setCurveCalculationConfig(Currency.JPY, "DefaultTwoCurveJPYConfig");
    securityGenerator.setCurveCalculationConfig(Currency.USD, "DefaultTwoCurveUSDConfig");
    securityGenerator.setPreferredScheme(ExternalSchemes.BLOOMBERG_TICKER);
    securityGenerator.setSpotRateIdentifier(new BiFunction<Currency, Currency, ExternalId>() {
      @Override
      public ExternalId apply(Currency a, Currency b) {
        return ExternalSchemes.bloombergTickerSecurityId(a.getCode() + b.getCode() + " Curncy");
      }
    });
  }
}
