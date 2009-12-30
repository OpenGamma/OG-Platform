/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.financial.Currency;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class NewDiscountCurveDefinition implements Serializable {
  private final Currency _currency;
  private final String _name;
  private final SortedSet<NewFixedIncomeStrip> _strips = new TreeSet<NewFixedIncomeStrip>();
  
  public NewDiscountCurveDefinition(Currency currency, String name) {
    this(currency, name, null);
  }
  
  public NewDiscountCurveDefinition(Currency currency, String name, Collection<? extends NewFixedIncomeStrip> strips) {
    ArgumentChecker.checkNotNull(currency, "Currency");
    // Name can be null.
    _currency = currency;
    _name = name;
    if(strips != null) {
      for(NewFixedIncomeStrip strip : strips) {
        addStrip(strip);
      }
    }
  }
  
  public void addStrip(NewFixedIncomeStrip strip) {
    ArgumentChecker.checkNotNull(strip, "Strip");
    _strips.add(strip);
  }

  /**
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * @return the name
   */
  public String getName() {
    return _name;
  }

  /**
   * @return the strips
   */
  public SortedSet<NewFixedIncomeStrip> getStrips() {
    return Collections.unmodifiableSortedSet(_strips);
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null) {
      return false;
    }
    if(!(obj instanceof NewDiscountCurveDefinition)) {
      return false;
    }
    NewDiscountCurveDefinition other = (NewDiscountCurveDefinition) obj;
    if(!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if(!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    if(!ObjectUtils.equals(_strips, other._strips)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int prime = 37;
    int result = 1;
    result = (result * prime) + _currency.hashCode();
    if(_name != null) {
      result = (result * prime) + _name.hashCode(); 
    }
    for(NewFixedIncomeStrip strip : _strips) {
      result = (result * prime) + strip.hashCode();
    }
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
