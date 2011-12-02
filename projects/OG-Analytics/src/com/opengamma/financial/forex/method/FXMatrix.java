/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

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
  private double[][] _fxrates;
  private int _nbCurrencies;

  /**
   * Constructor. The FXMatrix constructed has no currency and no fx rates.
   */
  public FXMatrix() {
    _currencies = new HashMap<Currency, Integer>();
    _fxrates = new double[0][0];
    _nbCurrencies = 0;
  }

  /**
   * Constructor with an initial currency pair.
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @param fxRate TheFX rate between ccy1 and the ccy2. It is 1 ccy1 = fxRate * ccy2. The FX matrix will be completed with the ccy2/ccy1 rate.
   */
  public FXMatrix(final Currency ccy1, final Currency ccy2, final double fxRate) {
    _currencies = new HashMap<Currency, Integer>();
    _fxrates = new double[0][0];
    this.addCurrency(ccy1, ccy2, fxRate);
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
    Validate.notNull(ccyToAdd, "Currency to add to the FX matrix should not be null");
    Validate.notNull(ccyReference, "Reference currency should not be null");
    Validate.isTrue(!ccyToAdd.equals(ccyReference), "Currencies should be different");
    if (_nbCurrencies == 0) { // FX Matrix is empty. 
      _currencies.put(ccyReference, 0);
      _currencies.put(ccyToAdd, 1);
      _fxrates = new double[2][2];
      _fxrates[0][0] = 1.0;
      _fxrates[1][1] = 1.0;
      _fxrates[1][0] = fxRate;
      _fxrates[0][1] = 1.0 / fxRate;
      _nbCurrencies = 2;
    } else {
      Validate.isTrue(_currencies.containsKey(ccyReference), "Reference currency not in the FX matrix");
      Validate.isTrue(!_currencies.containsKey(ccyToAdd), "New currency already in the FX matrix");
      _currencies.put(ccyToAdd, _nbCurrencies);
      _nbCurrencies++;
      double[][] fxRatesNew = new double[_nbCurrencies][_nbCurrencies];
      // Copy the previous matrix
      for (int loopccy = 0; loopccy < _nbCurrencies - 1; loopccy++) {
        System.arraycopy(_fxrates[loopccy], 0, fxRatesNew[loopccy], 0, _nbCurrencies - 1);
      }
      fxRatesNew[_nbCurrencies - 1][_nbCurrencies - 1] = 1.0;
      int indexRef = _currencies.get(ccyReference);
      for (int loopccy = 0; loopccy < _nbCurrencies - 1; loopccy++) {
        fxRatesNew[_nbCurrencies - 1][loopccy] = fxRate * _fxrates[indexRef][loopccy];
        fxRatesNew[loopccy][_nbCurrencies - 1] = 1.0 / fxRatesNew[_nbCurrencies - 1][loopccy];
      }
      _fxrates = fxRatesNew;
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
    Validate.notNull(index1, "Currency 1 not in the FX Matrix.");
    Validate.notNull(index2, "Currency 2 not in the FX Matrix.");
    return _fxrates[index1][index2];
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
    Validate.isTrue(_currencies.containsKey(ccyReference), "Reference currency not in the FX matrix");
    Validate.isTrue(_currencies.containsKey(ccyToUpdate), "Currency to update not in the FX matrix");
    int indexUpdate = _currencies.get(ccyToUpdate);
    int indexRef = _currencies.get(ccyReference);
    for (int loopccy = 0; loopccy < _nbCurrencies; loopccy++) {
      _fxrates[indexUpdate][loopccy] = fxRate * _fxrates[indexRef][loopccy];
      _fxrates[loopccy][indexUpdate] = 1.0 / _fxrates[indexUpdate][loopccy];
    }
    _fxrates[indexUpdate][indexUpdate] = 1.0;
  }

}
