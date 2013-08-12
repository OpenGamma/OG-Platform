/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Utility class for providers. This is a temporary class and will be removed when the providers
 * have been refactored.
 */
public class ProviderUtils {

  /**
   * Merges providers.
   * @param providers The providers to merge, not null or empty
   * @return The merged providers
   */
  public static MulticurveProviderDiscount merge(final Collection<MulticurveProviderDiscount> providers) {
    ArgumentChecker.notNull(providers, "providers");
    ArgumentChecker.notEmpty(providers, "providers");
    final MulticurveProviderDiscount result = new MulticurveProviderDiscount();
    for (final MulticurveProviderDiscount provider : providers) {
      for (final Map.Entry<Currency, YieldAndDiscountCurve> entry : provider.getDiscountingCurves().entrySet()) {
        result.setCurve(entry.getKey(), entry.getValue());
      }
      for (final Map.Entry<IborIndex, YieldAndDiscountCurve> entry : provider.getForwardIborCurves().entrySet()) {
        result.setCurve(entry.getKey(), entry.getValue());
      }
      for (final Map.Entry<IndexON, YieldAndDiscountCurve> entry : provider.getForwardONCurves().entrySet()) {
        result.setCurve(entry.getKey(), entry.getValue());
      }
      final FXMatrix matrix = provider.getFxRates();
      final Collection<Currency> currencies = matrix.getCurrencies().keySet();
      final Iterator<Currency> iterator = currencies.iterator();
      if (currencies.size() > 0) {
        final Currency initialCurrency = iterator.next();
        while (iterator.hasNext()) {
          final Currency otherCurrency = iterator.next();
          result.getFxRates().addCurrency(initialCurrency, otherCurrency, matrix.getFxRate(initialCurrency, otherCurrency));
        }
      }
    }
    return result;
  }

  /**
   * Merges a provider and an FX matrix
   * @param provider The provider, not null
   * @param matrix The FX matrix, not null
   * @return The merged provider
   */
  public static MulticurveProviderDiscount merge(final MulticurveProviderDiscount provider, final FXMatrix matrix) {
    ArgumentChecker.notNull(provider, "provider");
    ArgumentChecker.notNull(matrix, "matrix");
    final MulticurveProviderDiscount result = provider.copy();
    final Collection<Currency> currencies = matrix.getCurrencies().keySet();
    final Iterator<Currency> iterator = currencies.iterator();
    if (currencies.size() > 0) {
      final Currency initialCurrency = iterator.next();
      while (iterator.hasNext()) {
        final Currency otherCurrency = iterator.next();
        result.getFxRates().addCurrency(initialCurrency, otherCurrency, matrix.getFxRate(initialCurrency, otherCurrency));
      }
    }
    return result;
  }
}
