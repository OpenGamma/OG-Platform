/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Class describing a set of currencies and all the cross rates between them.
 */
public class FXMatrix {

  /**
   * The map between the currencies and their order.
   */
  private final Map<Currency, Integer> _currencies;
  /**
   * The matrix with all exchange rates. The entry [i][j] is such that 1.0 * Currency[i] = _fxrate * Currency[j].
   * If _currencies.get(EUR) = 0 and _currencies.get(USD) = 1, the element _fxRate[0][1] is likely to be something like 1.40 and _fxRate[1][0] like 0.7142... 
   * (the rate _fxRate[1][0] will be computed from _fxRate[0][1] when the object is constructed or updated).
   * All the element of the matrix are meaningful and coherent (the matrix is always completed in a coherent way when a currency is added or a rate updated).
   */
  private double[][] _fxRates;
  /**
   * The number of currencies.
   */
  private int _nbCurrencies;

  /**
   * Constructor with no currency. The FXMatrix constructed has no currency and no fx rates.
   */
  public FXMatrix() {
    _currencies = new LinkedHashMap<Currency, Integer>();
    _fxRates = new double[0][0];
    _nbCurrencies = 0;
  }

  /**
   * Constructor with one currency. The FXMatrix has one currency with a 1.0 exchange rate to itself.
   * @param ccy The currency.
   */
  public FXMatrix(final Currency ccy) {
    ArgumentChecker.notNull(ccy, "Currency");
    _currencies = new LinkedHashMap<Currency, Integer>();
    _currencies.put(ccy, 0);
    _fxRates = new double[1][1];
    _fxRates[0][0] = 1.0;
    _nbCurrencies = 1;
  }

  /**
   * Constructor with an initial currency pair.
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @param fxRate TheFX rate between ccy1 and the ccy2. It is 1 ccy1 = fxRate * ccy2. The FX matrix will be completed with the ccy2/ccy1 rate.
   */
  public FXMatrix(final Currency ccy1, final Currency ccy2, final double fxRate) {
    _currencies = new LinkedHashMap<Currency, Integer>();
    _fxRates = new double[0][0];
    this.addCurrency(ccy1, ccy2, fxRate);
  }

  /**
   * Constructor from an existing FXMatrix. A new map and array are created. 
   * @param fxMatrix The FXMatrix.
   */
  public FXMatrix(final FXMatrix fxMatrix) {
    ArgumentChecker.notNull(fxMatrix, "FXMatrix");
    _nbCurrencies = fxMatrix._nbCurrencies;
    _currencies = new LinkedHashMap<Currency, Integer>(fxMatrix._currencies);
    _fxRates = new double[_nbCurrencies][];
    for (int loopc = 0; loopc < _nbCurrencies; loopc++) {
      _fxRates[loopc] = fxMatrix._fxRates[loopc].clone();
    }
  }

  /**
   * Add a new currency to the FX matrix.
   * @param ccyToAdd The currency to add. Should not be in the FX matrix already.
   * @param ccyReference The reference currency used to compute the cross rates with the new currency. Should already be in the matrix, except if the matrix is empty. 
   * IF the FX matrix is empty, the reference currency will be used as currency 0.
   * @param fxRate TheFX rate between the new currency and the reference currency. It is 1 ccyToAdd = fxrate ccyReference. The FX matrix will be completed using cross rate
   * coherent with the data provided.
   */
  public void addCurrency(final Currency ccyToAdd, final Currency ccyReference, final double fxRate) {
    ArgumentChecker.notNull(ccyToAdd, "Currency to add to the FX matrix should not be null");
    ArgumentChecker.notNull(ccyReference, "Reference currency should not be null");
    ArgumentChecker.isTrue(!ccyToAdd.equals(ccyReference), "Currencies should be different");
    if (_nbCurrencies == 0) { // FX Matrix is empty. 
      _currencies.put(ccyReference, 0);
      _currencies.put(ccyToAdd, 1);
      _fxRates = new double[2][2];
      _fxRates[0][0] = 1.0;
      _fxRates[1][1] = 1.0;
      _fxRates[1][0] = fxRate;
      _fxRates[0][1] = 1.0 / fxRate;
      _nbCurrencies = 2;
    } else {
      Validate.isTrue(_currencies.containsKey(ccyReference), "Reference currency not in the FX matrix");
      Validate.isTrue(!_currencies.containsKey(ccyToAdd), "New currency already in the FX matrix");
      _currencies.put(ccyToAdd, _nbCurrencies);
      _nbCurrencies++;
      double[][] fxRatesNew = new double[_nbCurrencies][_nbCurrencies];
      // Copy the previous matrix
      for (int loopccy = 0; loopccy < _nbCurrencies - 1; loopccy++) {
        System.arraycopy(_fxRates[loopccy], 0, fxRatesNew[loopccy], 0, _nbCurrencies - 1);
      }
      fxRatesNew[_nbCurrencies - 1][_nbCurrencies - 1] = 1.0;
      int indexRef = _currencies.get(ccyReference);
      for (int loopccy = 0; loopccy < _nbCurrencies - 1; loopccy++) {
        fxRatesNew[_nbCurrencies - 1][loopccy] = fxRate * _fxRates[indexRef][loopccy];
        fxRatesNew[loopccy][_nbCurrencies - 1] = 1.0 / fxRatesNew[_nbCurrencies - 1][loopccy];
      }
      _fxRates = fxRatesNew;
    }
  }

  /**
   * Return the exchange rate between two currencies.
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @return The exchange rate: 1.0 * ccy1 = x * ccy2.
   */
  public double getFxRate(final Currency ccy1, final Currency ccy2) {
    Integer index1 = _currencies.get(ccy1);
    Integer index2 = _currencies.get(ccy2);
    ArgumentChecker.notNull(index1, "Currency 1 not in the FX Matrix.");
    ArgumentChecker.notNull(index2, "Currency 2 not in the FX Matrix.");
    return _fxRates[index1][index2];
  }

  /**
   * Convert a multiple currency amount into a amount in a given currency.
   * @param amount The multiple currency amount.
   * @param ccy The currency for the conversion.
   * @return The amount.
   */
  public CurrencyAmount convert(final MultipleCurrencyAmount amount, final Currency ccy) {
    double convertion = 0;
    CurrencyAmount[] ca = amount.getCurrencyAmounts();
    for (int loopccy = 0; loopccy < ca.length; loopccy++) {
      convertion += ca[loopccy].getAmount() * getFxRate(ca[loopccy].getCurrency(), ccy);
    }
    return CurrencyAmount.of(ccy, convertion);
  }

  /**
   * Reset the exchange rate of a given currency.
   * @param ccyToUpdate The currency for which the exchange rats should be updated. Should be in the FX matrix already.
   * @param ccyReference The reference currency used to compute the cross rates with the new currency. Should already be in the matrix.
   * @param fxRate TheFX rate between the new currency and the reference currency. It is 1.0 * ccyToAdd = fxrate * ccyReference. The FX matrix will be changed for currency1
   * using cross rate coherent with the data provided.
   */
  public void updateRates(final Currency ccyToUpdate, final Currency ccyReference, final double fxRate) {
    ArgumentChecker.isTrue(_currencies.containsKey(ccyReference), "Reference currency not in the FX matrix");
    ArgumentChecker.isTrue(_currencies.containsKey(ccyToUpdate), "Currency to update not in the FX matrix");
    int indexUpdate = _currencies.get(ccyToUpdate);
    int indexRef = _currencies.get(ccyReference);
    for (int loopccy = 0; loopccy < _nbCurrencies; loopccy++) {
      _fxRates[indexUpdate][loopccy] = fxRate * _fxRates[indexRef][loopccy];
      _fxRates[loopccy][indexUpdate] = 1.0 / _fxRates[indexUpdate][loopccy];
    }
    _fxRates[indexUpdate][indexUpdate] = 1.0;
  }

  @Override
  public String toString() {
    return _currencies.keySet().toString() + " - " + _fxRates;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currencies.hashCode();
    result = prime * result + Arrays.hashCode(_fxRates);
    result = prime * result + _nbCurrencies;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    FXMatrix other = (FXMatrix) obj;
    if (!ObjectUtils.equals(_currencies, other._currencies)) {
      return false;
    }
    if (!Arrays.deepEquals(_fxRates, other._fxRates)) {
      return false;
    }
    if (_nbCurrencies != other._nbCurrencies) {
      return false;
    }
    return true;
  }

}
