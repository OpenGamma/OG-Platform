/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.INTEGRATED_VARIANCE;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.LINEAR_TIME;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.LOG_TIME;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.MONEYNESS;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.STRIKE;
import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityPDEValuePropertyNames.VOLATILITY;

import com.opengamma.OpenGammaRuntimeException;

/**
 * 
 */
public class LocalVolatilityPDEUtils {

  static boolean isMoneynessSurface(final String property) {
    if (property.equals(MONEYNESS)) {
      return true;
    }
    if (property.equals(STRIKE)) {
      return false;
    }
    throw new OpenGammaRuntimeException("Could not recognise moneyness property " + property);
  }

  static boolean useLogTime(final String property) {
    if (property.equals(LOG_TIME)) {
      return true;
    }
    if (property.equals(LINEAR_TIME)) {
      return true;
    }
    throw new OpenGammaRuntimeException("Could not recognise x-axis property " + property);
  }

  static boolean useIntegratedVariance(final String property) {
    if (property.equals(INTEGRATED_VARIANCE)) {
      return true;
    }
    if (property.equals(VOLATILITY)) {
      return false;
    }
    throw new OpenGammaRuntimeException("Could not recognise y-axis property " + property);
  }
}
