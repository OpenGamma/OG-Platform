/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.forex;

import com.opengamma.analytics.financial.model.option.definition.SmileDeltaParameters;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParameters;
import com.opengamma.util.money.Currency;

/**
 * Interface to Forex volatility smile described from delta and multi-curves provider.
 */
public interface BlackForexVannaVolgaProviderInterface extends BlackForexProviderInterface<SmileDeltaTermStructureParameters> {

  /**
    * Returns the (Black implied) volatility smile at a given expiration time.
    * @param ccy1 The first currency.
    * @param ccy2 The second currency.
    * @param time The time to expiration.
    * @return The volatility smile.
    */
  SmileDeltaParameters getSmile(final Currency ccy1, final Currency ccy2, final double time);

}
