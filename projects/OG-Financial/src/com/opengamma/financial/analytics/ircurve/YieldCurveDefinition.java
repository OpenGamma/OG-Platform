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

import com.opengamma.core.common.Currency;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 */
public class YieldCurveDefinition implements Serializable, UniqueIdentifiable, MutableUniqueIdentifiable {
  private UniqueIdentifier _uniqueIdentifier;
  private final Currency _currency;
  private final String _name;
  private final String _interpolatorName;
  private final SortedSet<FixedIncomeStrip> _strips = new TreeSet<FixedIncomeStrip>();
  
  public YieldCurveDefinition(Currency currency, String name, String interpolatorName) {
    this(currency, name, interpolatorName, null);
  }
  
  public YieldCurveDefinition(Currency currency, String name, String interpolatorName, Collection<? extends FixedIncomeStrip> strips) {
    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(interpolatorName, "Interpolator name");
    // Name can be null.
    _currency = currency;
    _name = name;
    _interpolatorName = interpolatorName;
    if (strips != null) {
      for (FixedIncomeStrip strip : strips) {
        addStrip(strip);
      }
    }
  }

  public void addStrip(FixedIncomeStrip strip) {
    ArgumentChecker.notNull(strip, "Strip");
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
   * @return the interpolatorName
   */
  public String getInterpolatorName() {
    return _interpolatorName;
  }

  /**
   * @return the strips
   */
  public SortedSet<FixedIncomeStrip> getStrips() {
    return Collections.unmodifiableSortedSet(_strips);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof YieldCurveDefinition)) {
      return false;
    }
    YieldCurveDefinition other = (YieldCurveDefinition) obj;
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    if (!ObjectUtils.equals(_interpolatorName, other._interpolatorName)) {
      return false;
    }
    if (!ObjectUtils.equals(_strips, other._strips)) {
      return false;
    }
    if (!ObjectUtils.equals(_uniqueIdentifier, other._uniqueIdentifier)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int prime = 37;
    int result = 1;
    result = (result * prime) + _currency.hashCode();
    if (_name != null) {
      result = (result * prime) + _name.hashCode();
    }
    if (_interpolatorName != null) {
      result = (result * prime) + _interpolatorName.hashCode();
    }
    for (FixedIncomeStrip strip : _strips) {
      result = (result * prime) + strip.hashCode();
    }
    result *= prime;
    if (_uniqueIdentifier != null) {
      result += _uniqueIdentifier.hashCode();
    }
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public UniqueIdentifier getUniqueId() {
    return _uniqueIdentifier;
  }

  @Override
  public void setUniqueId(final UniqueIdentifier uniqueIdentifier) {
    _uniqueIdentifier = uniqueIdentifier;
  }

}
