/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.generator;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.id.ExternalId;
import com.opengamma.util.functional.Function2;
import com.opengamma.util.money.Currency;

/**
 * Portfolio generator for Bloomberg exmaples.
 */
public class PortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {

  protected void configureChain(final SecurityGenerator<?> securityGenerator) {
    super.configureChain(securityGenerator);
    securityGenerator.setCurrencyCurveName("FUNDING");
    securityGenerator.setPreferredScheme(SecurityUtils.BLOOMBERG_TICKER);
    securityGenerator.setSpotRateIdentifier(new Function2<Currency, Currency, ExternalId>() {
      @Override
      public ExternalId execute(final Currency a, final Currency b) {
        return SecurityUtils.bloombergTickerSecurityId(a.getCode() + b.getCode() + " Curncy");
      }
    });
  }
}
  
