/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.horizon;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class HorizonUtils {

  public static Double getNonZeroValue(final MultipleCurrencyAmount mca) {
    Double result = null;
    for (final CurrencyAmount ca : mca) {
      if (Double.doubleToLongBits(ca.getAmount()) != 0L) {
        if (result != null) {
          throw new OpenGammaRuntimeException("Already have a non-zero value");
        }
        result = ca.getAmount();
      }
    }
    return result;
  }
}
