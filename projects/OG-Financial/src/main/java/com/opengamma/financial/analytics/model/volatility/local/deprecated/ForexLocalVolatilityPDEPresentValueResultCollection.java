/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local.deprecated;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.forex.conversion.ForexDomesticPipsToPresentValueConverter;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 * @deprecated Deprecated
 */
@Deprecated
public class ForexLocalVolatilityPDEPresentValueResultCollection {
  /** Label for pips PV quotes calculated using local volatility */
  public static final String LV_PIPS = "Local Volatility Pips";
  /** Label for put PV quotes calculated using local volatility */
  public static final String LV_PUT_PV = "Local Volatility Put Present Value";
  /** Label for call PV quotes calculated using local volatility */
  public static final String LV_CALL_PV = "Local Volatility Call Present Value";
  /** Label for pips PV quotes calculated using the Black model */
  public static final String BLACK_PIPS = "Black Pips";
  /** Label for put PV quotes calculated using the Black model */
  public static final String BLACK_PUT_PV = "Black Put Present Value";
  /** Label for call PV quotes calculated using the Black model */
  public static final String BLACK_CALL_PV = "Black Call Present Value";
  private final Map<String, double[]> _pvDataMap = new TreeMap<String, double[]>();
  private final double[] _strikes;
  private final int _n;

  public ForexLocalVolatilityPDEPresentValueResultCollection(final double[] strikes, final double[] lvPutPips, final double[] blackPutPips, final double spotFX, final Currency putCurrency,
      final Currency callCurrency, final double putAmount, final double callAmount) {
    ArgumentChecker.notNull(strikes, "strikes");
    ArgumentChecker.notNull(lvPutPips, "LV put pips");
    ArgumentChecker.notNull(blackPutPips, "Black put pips");
    ArgumentChecker.notNull(putCurrency, "put currency");
    ArgumentChecker.notNull(callCurrency, "call currency");
    _n = strikes.length;
    ArgumentChecker.isTrue(_n == lvPutPips.length, "Number of expiries must equal number of LV put pips; have {} and {}", _n, lvPutPips.length);
    ArgumentChecker.isTrue(_n == blackPutPips.length, "Number of expiries must equal number of Black put pips; have {} and {}", _n, blackPutPips.length);
    _strikes = strikes;
    _pvDataMap.put(LV_PIPS, lvPutPips);
    final double[] lvPutPV = new double[_n];
    final double[] lvCallPV = new double[_n];
    _pvDataMap.put(BLACK_PIPS, blackPutPips);
    final double[] blackPutPV = new double[_n];
    final double[] blackCallPV = new double[_n];
    for (int i = 0; i < _n; i++) {
      MultipleCurrencyAmount mca = ForexDomesticPipsToPresentValueConverter.convertDomesticPipsToFXPresentValue(lvPutPips[i], spotFX, putCurrency, callCurrency, putAmount, callAmount);
      lvPutPV[i] = mca.getAmount(callCurrency);
      lvCallPV[i] = mca.getAmount(putCurrency);
      mca = ForexDomesticPipsToPresentValueConverter.convertDomesticPipsToFXPresentValue(blackPutPips[i], spotFX, putCurrency, callCurrency, putAmount, callAmount);
      blackPutPV[i] = mca.getAmount(callCurrency);
      blackCallPV[i] = mca.getAmount(putCurrency);
    }
    _pvDataMap.put(LV_PUT_PV, lvPutPV);
    _pvDataMap.put(LV_CALL_PV, lvCallPV);
    _pvDataMap.put(BLACK_PUT_PV, blackPutPV);
    _pvDataMap.put(BLACK_CALL_PV, blackCallPV);
  }

  public ForexLocalVolatilityPDEPresentValueResultCollection(final double[] strikes, final Map<String, double[]> pvDataMap) {
    ArgumentChecker.notNull(strikes, "strikes");
    ArgumentChecker.notNull(pvDataMap, "PV data map");
    _strikes = strikes;
    _n = strikes.length;
    _pvDataMap.putAll(pvDataMap);
  }

  public double[] getPV(final String name) {
    ArgumentChecker.notNull(name, "name");
    return _pvDataMap.get(name);
  }

  public Double getPointPV(final String name, final double strike, final Interpolator1D interpolator) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(interpolator, "interpolator");
    if (!(_pvDataMap.containsKey(name)) || _pvDataMap.get(name) == null) {
      return null;
    }
    final Interpolator1DDataBundle data = interpolator.getDataBundle(_strikes, _pvDataMap.get(name));
    return interpolator.interpolate(data, strike);
  }

  public double[] getStrikes() {
    return _strikes;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _pvDataMap.hashCode();
    result = prime * result + Arrays.hashCode(_strikes);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ForexLocalVolatilityPDEPresentValueResultCollection other = (ForexLocalVolatilityPDEPresentValueResultCollection) obj;
    if (!ObjectUtils.equals(_pvDataMap, other._pvDataMap)) {
      return false;
    }
    if (!Arrays.equals(_strikes, other._strikes)) {
      return false;
    }
    return true;
  }

}
