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
 * Factory object that creates {@link ExposureFunction} instances for a given name.
 */
public class ExposureFunctionFactory {

  /**
   * Returns the {@link ExposureFunction} implementation for a given name.
   * @param securitySource the security source, which may or may not be used, not null.
   * @param name the name of the exposure function, not null.
   * @return the exposure function implementation.
   */
  public static ExposureFunction getExposureFunction(final SecuritySource securitySource, final String name) {
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(name, "name");
    switch (name) {
      case ContractCategoryExposureFunction.NAME:
        return new ContractCategoryExposureFunction(securitySource);
      case CounterpartyExposureFunction.NAME:
        return new CounterpartyExposureFunction();
      case CurrencyExposureFunction.NAME:
        return new CurrencyExposureFunction(securitySource);
      case RegionExposureFunction.NAME:
        return new RegionExposureFunction(securitySource);
      case SecurityAndCurrencyExposureFunction.NAME:
        return new SecurityAndCurrencyExposureFunction(securitySource);
      case SecurityAndRegionExposureFunction.NAME:
        return new SecurityAndRegionExposureFunction(securitySource);
      case SecurityAndSettlementExchangeExposureFunction.NAME:
        return new SecurityAndSettlementExchangeExposureFunction();
      case SecurityAndTradingExchangeExposureFunction.NAME:
        return new SecurityAndTradingExchangeExposureFunction();
      case SecurityExposureFunction.NAME:
        return new SecurityExposureFunction();
      case SecurityTypeExposureFunction.NAME:
        return new SecurityTypeExposureFunction();
      case UnderlyingExposureFunction.NAME:
        return new UnderlyingExposureFunction(securitySource);
      default:
        throw new OpenGammaRuntimeException("Could not get exposure function called " + name);
    }
  }

}
