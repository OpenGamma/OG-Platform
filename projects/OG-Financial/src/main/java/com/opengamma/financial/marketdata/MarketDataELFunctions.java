/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdata;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.CurveShiftFunctionFactory;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.expression.UserExpressionParser;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.StandardCurrencyPairs;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Static functions for the EL compiler.
 */
public final class MarketDataELFunctions {

  private MarketDataELFunctions() {
  }

  public static Security getSecurity(final Object id) {
    return UserExpressionParser.resolve(ComputationTargetType.SECURITY, id);
  }

  public static Object pointShiftCurve(final Object curve, final double x, final double shift) {
    if (curve instanceof YieldCurve) {
      final DoublesCurve shifted = CurveShiftFunctionFactory.getShiftedCurve(((YieldCurve) curve).getCurve(), x, shift);
      return YieldCurve.from(shifted);
    } else {
      throw new UnsupportedOperationException("Invalid curve - " + curve);
    }
  }

  public static Object parallelShiftCurve(final Object curve, final double shift) {
    if (curve instanceof YieldCurve) {
      final DoublesCurve shifted = CurveShiftFunctionFactory.getShiftedCurve(((YieldCurve) curve).getCurve(), shift);
      return YieldCurve.from(shifted);
    } else {
      throw new UnsupportedOperationException("Invalid curve - " + curve);
    }
  }

  public static double getFXMultiplier(final Object id, final double multiplier) {
    if (id instanceof ExternalId) {
      return getFXMultiplierFromExternalId((ExternalId) id, multiplier);
    } else if (id instanceof ExternalIdBundle) {
      final ExternalIdBundle bundle = (ExternalIdBundle) id;
      if (bundle.getExternalId(ExternalSchemes.BLOOMBERG_TICKER) != null) {
        return getFXMultiplierFromExternalId(bundle.getExternalId(ExternalSchemes.BLOOMBERG_TICKER), multiplier);
      } else if (bundle.getExternalId(ExternalSchemes.BLOOMBERG_TICKER_WEAK) != null) {
        return getFXMultiplierFromExternalId(bundle.getExternalId(ExternalSchemes.BLOOMBERG_TICKER_WEAK), multiplier);
      }
    } else if (id instanceof UniqueId) {
      final UniqueId uid = (UniqueId) id;
      return getFXMultiplierFromExternalId(ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, uid.getValue()), multiplier);
    }
    throw new OpenGammaRuntimeException("id was not FX rate, which shouldn't happen");
  }

  private static double getFXMultiplierFromExternalId(final ExternalId id, final double multiplier) {
    final Pair<Currency, Currency> pair = getFXPairFromBloombergTicker(id);
    if (StandardCurrencyPairs.isStandardPair(pair.getFirst(), pair.getSecond())) {
      return multiplier;
    } else {
      return 1 / multiplier;
    }
  }

  private static Pair<Currency, Currency> getFXPairFromBloombergTicker(final ExternalId id) {
    if (id.getScheme().equals(ExternalSchemes.BLOOMBERG_TICKER) || id.getScheme().equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK)) {
      final String ticker = id.getValue();
      final String[] split = ticker.split(" ");
      if (split.length != 2) {
        throw new OpenGammaRuntimeException("ticker contained more than one space:" + ticker);
      }
      if (!split[1].equals("Curncy")) {
        throw new OpenGammaRuntimeException("ticker did not end with Curncy:" + ticker);
      }
      final String ccyPart = split[0];
      switch (ccyPart.length()) {
        case 3:
        {
          final Currency ccy = Currency.of(ccyPart);
          if (StandardCurrencyPairs.isSingleCurrencyNumerator(ccy)) {
            return Pairs.of(ccy, Currency.USD);
          } else {
            return Pairs.of(Currency.USD, ccy);
          }
        }
        case 6:
        {
          final Currency numerator = Currency.of(ccyPart.substring(0, 3));
          final Currency denominator = Currency.of(ccyPart.substring(3));
          return Pairs.of(numerator, denominator);
        }
        default:
          throw new OpenGammaRuntimeException("currency part of ticker did not have 3 or 6 characters" + ticker);
      }
    } else {
      throw new OpenGammaRuntimeException("id was not bloomberg ticker or bloomberg weak ticker" + id);
    }
  }

  public static boolean isFXRate(final Object id) {
    if (id instanceof ExternalId) {
      return isFXRateFromBloombergTicker((ExternalId) id);
    } else if (id instanceof ExternalIdBundle) {
      final ExternalIdBundle bundle = (ExternalIdBundle) id;
      if (bundle.getExternalId(ExternalSchemes.BLOOMBERG_TICKER) != null) {
        return isFXRateFromBloombergTicker(bundle.getExternalId(ExternalSchemes.BLOOMBERG_TICKER));
      } else if (bundle.getExternalId(ExternalSchemes.BLOOMBERG_TICKER_WEAK) != null) {
        return isFXRateFromBloombergTicker(bundle.getExternalId(ExternalSchemes.BLOOMBERG_TICKER_WEAK));
      }
    } else if (id instanceof UniqueId) {
      final UniqueId uid = (UniqueId) id;
      return isFXRateFromBloombergTicker(ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, uid.getValue()));
    }
    return false;
  }

  private static boolean isFXRateFromBloombergTicker(final ExternalId id) {
    if (id.getScheme().equals(ExternalSchemes.BLOOMBERG_TICKER) || id.getScheme().equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK)) {
      final String ticker = id.getValue();
      final String[] split = ticker.split(" ");
      if (split.length != 2) {
        return false;
      }
      if (!split[1].equals("Curncy")) {
        return false;
      }
      final String ccyPart = split[0];
      switch (ccyPart.length()) {
        case 3:
          return true;
        case 6:
          return true;
        default:
          return false;
      }
    } else {
      return false;
    }
  }

}
