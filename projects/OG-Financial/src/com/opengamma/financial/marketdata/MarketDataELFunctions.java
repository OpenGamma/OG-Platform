/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdata;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.math.curve.Curve;
import com.opengamma.math.curve.CurveShiftFunctionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.StandardCurrencyPairs;
import com.opengamma.util.tuple.Pair;

/**
 * Static functions for the EL compiler.
 * <p>
 * Note that the EL evaluation requires static methods, but we need to associate state (e.g. which
 * security source to use for getSecurity). An evaluator instance must hold the class monitor for
 * the duration of the evaluation and set the {@link #s_compiler} reference to itself.
 */
public final class MarketDataELFunctions {

  private static MarketDataELCompiler s_compiler;

  private MarketDataELFunctions() {
  }

  protected static MarketDataELCompiler getCompiler() {
    return s_compiler;
  }

  protected static void setCompiler(final MarketDataELCompiler compiler) {
    s_compiler = compiler;
  }

  public static Security getSecurity(final Object id) {
    if (id instanceof ExternalId) {
      return getCompiler().getSecuritySource().getSecurity(ExternalIdBundle.of((ExternalId) id));
    } else if (id instanceof UniqueId) {
      return getCompiler().getSecuritySource().getSecurity((UniqueId) id);
    } else if (id instanceof ExternalIdBundle) {
      return getCompiler().getSecuritySource().getSecurity((ExternalIdBundle) id);
    } else if (id instanceof Security) {
      return (Security) id;
    } else {
      throw new UnsupportedOperationException("Invalid ID - " + id);
    }
  }

  public static Object pointShiftCurve(final Object curve, final double x, final double shift) {
    if (curve instanceof YieldCurve) {
      final Curve<Double, Double> shifted = CurveShiftFunctionFactory.getShiftedCurve(((YieldCurve) curve).getCurve(), x, shift);
      return new YieldCurve(shifted);
    } else {
      throw new UnsupportedOperationException("Invalid curve - " + curve);
    }
  }

  public static Object parallelShiftCurve(final Object curve, final double shift) {
    if (curve instanceof YieldCurve) {
      final Curve<Double, Double> shifted = CurveShiftFunctionFactory.getShiftedCurve(((YieldCurve) curve).getCurve(), shift);
      return new YieldCurve(shifted);
    } else {
      throw new UnsupportedOperationException("Invalid curve - " + curve);
    }
  }
  
  public static double getFXMultiplier(final Object id, double multiplier) {
    if (id instanceof ExternalId) {
      return getFXMultiplierFromExternalId((ExternalId) id, multiplier);
    } else if (id instanceof ExternalIdBundle) {
      ExternalIdBundle bundle = (ExternalIdBundle) id;
      if (bundle.getExternalId(SecurityUtils.BLOOMBERG_TICKER) != null) {
        return getFXMultiplierFromExternalId(bundle.getExternalId(SecurityUtils.BLOOMBERG_TICKER), multiplier);
      } else if (bundle.getExternalId(SecurityUtils.BLOOMBERG_TICKER_WEAK) != null) {
        return getFXMultiplierFromExternalId(bundle.getExternalId(SecurityUtils.BLOOMBERG_TICKER_WEAK), multiplier);
      }
    } else if (id instanceof UniqueId) {
      UniqueId uid = (UniqueId) id;
      return getFXMultiplierFromExternalId(ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, uid.getValue()), multiplier);
    }
    throw new OpenGammaRuntimeException("id was not FX rate, which shouldn't happen");
  }
  
  private static double getFXMultiplierFromExternalId(final ExternalId id, double multiplier) {
    Pair<Currency, Currency> pair = getFXPairFromBloombergTicker(id);
    if (StandardCurrencyPairs.isStandardPair(pair.getFirst(), pair.getSecond())) {
      return multiplier;
    } else {
      return 1 / multiplier;
    }
  }
  
  private static Pair<Currency, Currency> getFXPairFromBloombergTicker(final ExternalId id) {
    if (id.getScheme().equals(SecurityUtils.BLOOMBERG_TICKER) || id.getScheme().equals(SecurityUtils.BLOOMBERG_TICKER_WEAK)) {
      String ticker = id.getValue();
      String[] split = ticker.split(" ");
      if (split.length != 2) {
        throw new OpenGammaRuntimeException("ticker contained more than one space:" + ticker);
      }
      if (!split[1].equals("Curncy")) {
        throw new OpenGammaRuntimeException("ticker did not end with Curncy:" + ticker);
      }
      String ccyPart = split[0];
      switch (ccyPart.length()) {
        case 3:
        {
          Currency ccy = Currency.of(ccyPart);
          if (StandardCurrencyPairs.isSingleCurrencyNumerator(ccy)) {
            return Pair.of(ccy, Currency.USD);
          } else {
            return Pair.of(Currency.USD, ccy);
          }
        }
        case 6:
        {
          Currency numerator = Currency.of(ccyPart.substring(0, 3));
          Currency denominator = Currency.of(ccyPart.substring(3));
          return Pair.of(numerator, denominator);
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
      ExternalIdBundle bundle = (ExternalIdBundle) id;
      if (bundle.getExternalId(SecurityUtils.BLOOMBERG_TICKER) != null) {
        return isFXRateFromBloombergTicker(bundle.getExternalId(SecurityUtils.BLOOMBERG_TICKER));
      } else if (bundle.getExternalId(SecurityUtils.BLOOMBERG_TICKER_WEAK) != null) {
        return isFXRateFromBloombergTicker(bundle.getExternalId(SecurityUtils.BLOOMBERG_TICKER_WEAK));
      }
    } else if (id instanceof UniqueId) {
      UniqueId uid = (UniqueId) id;
      return isFXRateFromBloombergTicker(ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, uid.getValue()));
    }
    return false;
  }
  
  private static boolean isFXRateFromBloombergTicker(final ExternalId id) {
    if (id.getScheme().equals(SecurityUtils.BLOOMBERG_TICKER) || id.getScheme().equals(SecurityUtils.BLOOMBERG_TICKER_WEAK)) {
      String ticker = id.getValue();
      String[] split = ticker.split(" ");
      if (split.length != 2) {
        return false;
      }
      if (!split[1].equals("Curncy")) {
        return false;
      }
      String ccyPart = split[0];
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
