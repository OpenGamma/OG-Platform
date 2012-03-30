/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.convention;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class ForexQuoteConvention {
  /** Quote conventions for ATM values */
  public enum ATMType {
    /** Forward */
    Forward,
    /** Delta-neutral straddle */
    DeltaNeutralStraddle
  }

  /** Quote conventions for delta values */
  public enum DeltaType {
    /** Spot */
    Spot,
    /** Forward */
    Forward
  }

  private final Currency _ccy1;
  private final Currency _ccy2;
  private final Currency _premiumCurrency;
  private final ATMType[] _atmType;
  private final Tenor _atmTypeCutoff;
  private final boolean _isPremiumAdjusted;
  private final DeltaType[] _deltaType;
  private final Tenor _deltaTypeCutoff;

  public ForexQuoteConvention(final Currency ccy1, final Currency ccy2, final boolean firstCurrencyDominates, final ATMType atmType,
      final boolean isPremiumAdjusted, final DeltaType deltaType) {
    ArgumentChecker.notNull(ccy1, "ccy1");
    ArgumentChecker.notNull(ccy2, "ccy2");
    ArgumentChecker.notNull(atmType, "ATM type");
    ArgumentChecker.notNull(deltaType, "delta type");
    _ccy1 = ccy1;
    _ccy2 = ccy2;
    _premiumCurrency = firstCurrencyDominates ? ccy1 : ccy2;
    _atmType = new ATMType[] {atmType };
    _atmTypeCutoff = null;
    _isPremiumAdjusted = isPremiumAdjusted;
    _deltaType = new DeltaType[] {deltaType };
    _deltaTypeCutoff = null;
  }

  public ForexQuoteConvention(final Currency ccy1, final Currency ccy2, final boolean firstCurrencyDominates, final ATMType atmTypeBelowCutoff,
      final Tenor atmTypeCutoff, final ATMType atmTypeAboveCutoff, final boolean isPremiumAdjusted, final DeltaType deltaTypeBelowCutoff,
      final Tenor deltaTypeCutoff, final DeltaType deltaTypeAboveCutoff) {
    ArgumentChecker.notNull(ccy1, "ccy1");
    ArgumentChecker.notNull(ccy2, "ccy2");
    ArgumentChecker.notNull(atmTypeBelowCutoff, "ATM type below cutoff");
    ArgumentChecker.notNull(atmTypeCutoff, "ATM type cutoff");
    ArgumentChecker.notNull(atmTypeAboveCutoff, "atm type above cutoff");
    ArgumentChecker.notNull(deltaTypeBelowCutoff, "delta type below cutoff");
    ArgumentChecker.notNull(deltaTypeCutoff, "delta type cutoff");
    ArgumentChecker.notNull(deltaTypeAboveCutoff, "delta type above cutoff");
    _ccy1 = ccy1;
    _ccy2 = ccy2;
    _premiumCurrency = firstCurrencyDominates ? ccy1 : ccy2;
    _atmType = new ATMType[] {atmTypeBelowCutoff, atmTypeAboveCutoff };
    _atmTypeCutoff = null;
    _isPremiumAdjusted = isPremiumAdjusted;
    _deltaType = new DeltaType[] {deltaTypeBelowCutoff, deltaTypeAboveCutoff };
    _deltaTypeCutoff = null;
  }

  public Currency getFirstCurrency() {
    return _ccy1;
  }

  public Currency getSecondCurrency() {
    return _ccy2;
  }

  public Currency getPremiumCurrency() {
    return _premiumCurrency;
  }

  public ATMType getATMQuoteType(final Tenor tenor) {
    if (_atmTypeCutoff == null) {
      return _atmType[0];
    }
    ArgumentChecker.notNull(tenor, "tenor");
    return _atmTypeCutoff.compareTo(tenor) < 0 ? _atmType[0] : _atmType[1];
  }

  public boolean isPremiumAdjusted() {
    return _isPremiumAdjusted;
  }

  public DeltaType getDeltaType(final Tenor tenor) {
    if (_deltaTypeCutoff == null) {
      return _deltaType[0];
    }
    ArgumentChecker.notNull(tenor, "tenor");
    return _deltaTypeCutoff.compareTo(tenor) < 0 ? _deltaType[0] : _deltaType[1];
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_atmType);
    result = prime * result + ((_atmTypeCutoff == null) ? 0 : _atmTypeCutoff.hashCode());
    result = prime * result + _ccy1.hashCode();
    result = prime * result + _ccy2.hashCode();
    result = prime * result + Arrays.hashCode(_deltaType);
    result = prime * result + ((_deltaTypeCutoff == null) ? 0 : _deltaTypeCutoff.hashCode());
    result = prime * result + (_isPremiumAdjusted ? 1231 : 1237);
    result = prime * result + _premiumCurrency.hashCode();
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
    final ForexQuoteConvention other = (ForexQuoteConvention) obj;
    if (!ObjectUtils.equals(_ccy1, other._ccy1)) {
      return false;
    }
    if (!ObjectUtils.equals(_ccy2, other._ccy2)) {
      return false;
    }
    if (!ObjectUtils.equals(_premiumCurrency, other._premiumCurrency)) {
      return false;
    }
    if (!Arrays.equals(_atmType, other._atmType)) {
      return false;
    }
    if (!Arrays.equals(_deltaType, other._deltaType)) {
      return false;
    }
    if (_isPremiumAdjusted != other._isPremiumAdjusted) {
      return false;
    }
    if (!ObjectUtils.equals(_atmTypeCutoff, other._atmTypeCutoff)) {
      return false;
    }
    if (!ObjectUtils.equals(_deltaTypeCutoff, other._deltaTypeCutoff)) {
      return false;
    }
    return true;
  }

}
