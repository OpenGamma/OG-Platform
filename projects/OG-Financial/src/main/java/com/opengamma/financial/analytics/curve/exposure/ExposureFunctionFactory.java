/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ExposureFunctionFactory {

  public static ExposureFunction getExposureFunction(final SecuritySource securitySource, final String name) {
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(name, "name");
    if ("Contract Category".equals(name)) {
      return new ContractCategoryExposureFunction(securitySource);
    }
    if ("Currency".equals(name)) {
      return new CurrencyExposureFunction(securitySource);
    }
    if ("Region".equals(name)) {
      return new RegionExposureFunction(securitySource);
    }
    if ("Security / Currency".equals(name)) {
      return new SecurityAndCurrencyExposureFunction(securitySource);
    }
    if ("Security / Region".equals(name)) {
      return new SecurityAndRegionExposureFunction(securitySource);
    }
    if ("Security / Settlement Exchange".equals(name)) {
      return new SecurityAndSettlementExchangeExposureFunction();
    }
    if ("Security / Trading Exchange".equals(name)) {
      return new SecurityAndTradingExchangeExposureFunction();
    }
    if ("Security".equals(name)) {
      return new SecurityExposureFunction();
    }
    if ("Security Type".equals(name)) {
      return new SecurityTypeExposureFunction();
    }
    if ("Underlying".equals(name)) {
      return new UnderlyingExposureFunction(securitySource);
    }
    throw new OpenGammaRuntimeException("Could not get exposure function called " + name);
  }

}
