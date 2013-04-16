/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.conversion;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public final class ForexDomesticPipsToPresentValueConverter {

  public static MultipleCurrencyAmount convertDomesticPipsToFXPresentValue(final double domesticPipsPV, final double spotFX, final Currency putCurrency, final Currency callCurrency,
      final double putAmount, final double callAmount) {
    ArgumentChecker.isTrue(domesticPipsPV >= 0.0, "Negative price given");
    ArgumentChecker.isTrue(spotFX > 0.0, "Spot rate must be greater than zero. value gvien is {}", spotFX);
    ArgumentChecker.notNull(putCurrency, "put currency");
    ArgumentChecker.notNull(callCurrency, "call currency");
    final Map<Currency, Double> amountMap = new HashMap<>();
    final double putPV = putAmount * domesticPipsPV;
    final double callPV = callAmount * domesticPipsPV / spotFX;
    amountMap.put(callCurrency, callPV);
    amountMap.put(putCurrency, putPV);
    final MultipleCurrencyAmount mca = MultipleCurrencyAmount.of(amountMap);
    return mca;
  }
}
