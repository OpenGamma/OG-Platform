/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local.deprecated;

import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.INTEGRATED_VARIANCE;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.LINEAR_TIME;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.LINEAR_Y;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.LOG_TIME;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.LOG_Y;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.MONEYNESS;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.STRIKE;
import static com.opengamma.financial.analytics.model.volatility.local.deprecated.LocalVolatilityPDEValuePropertyNames.VOLATILITY;

import com.opengamma.OpenGammaRuntimeException;

/**
 *
 * @deprecated Deprecated
 */
@Deprecated
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

  static boolean useLogValue(final String property) {
    if (property.equals(LOG_Y)) {
      return true;
    }
    if (property.equals(LINEAR_Y)) {
      return true;
    }
    throw new OpenGammaRuntimeException("Could not recognise y-axis property type " + property);
  }
}
