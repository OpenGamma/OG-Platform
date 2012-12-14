/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.method;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRCapProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Interface for interest rate pricing methods.
 */
public interface CapFloorIborSABRCapMethodInterface {

  /**
   * Computes the present value of the instrument.
   * @param cap The cap/floor on Ibor.
   * @param sabr The SABR cap/floor data and multi-curves provider.
   * @return The present value.
   */
  MultipleCurrencyAmount presentValue(final CapFloorIbor cap, final SABRCapProviderInterface sabr);

}
