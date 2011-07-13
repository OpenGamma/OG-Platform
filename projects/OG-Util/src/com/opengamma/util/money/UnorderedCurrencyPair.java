/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import org.apache.commons.lang.Validate;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;

/**
 * Stores a pair of currencies without any implied ordering
 * so, UnorderedCurrencyPair(USD, EUR) == UnorderedCurrencyPair(EUR, USD)
 */
public class UnorderedCurrencyPair implements UniqueIdentifiable {

  private static final String OBJECT_IDENTIFIER_SCHEME = "UnorderedCurrencyPair";
  private Currency _ccy1;
  private Currency _ccy2;
  private String _idValue;
  
  public UnorderedCurrencyPair(Currency ccy1, Currency ccy2) {
    Validate.notNull(ccy1, "Currency 1");
    Validate.notNull(ccy2, "Currency 2");
    _ccy1 = ccy1;
    _ccy2 = ccy2;
    if (_ccy1.getCode().compareTo(_ccy2.getCode()) >= 0) {
      _idValue = _ccy1.getCode() + ccy2.getCode(); 
    } else {
      _idValue = _ccy2.getCode() + ccy1.getCode();
    }
  }
  
  public Currency getFirstCurrency() {
    return _ccy1;
  }
  
  public Currency getSecondCurrency() {
    return _ccy2;
  }
  
  @Override
  public UniqueIdentifier getUniqueId() {
    return UniqueIdentifier.of(OBJECT_IDENTIFIER_SCHEME, _idValue);
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof UnorderedCurrencyPair)) {
      return false;
    }
    UnorderedCurrencyPair other = (UnorderedCurrencyPair) o;
    return (_idValue.equals(other._idValue));
  }
  
  public int hashCode() {
    return getFirstCurrency().hashCode() * (getSecondCurrency().hashCode());
  }

  public static UnorderedCurrencyPair of(Currency ccy1, Currency ccy2) {
    return new UnorderedCurrencyPair(ccy1, ccy2);
  }
}
