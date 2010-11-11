/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.core.common.Currency;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 */
public class YieldCurveDefinition implements Serializable {
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
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  // /CSOFF
  private static String CURRENCY_KEY = "currency";
  private static String NAME_KEY = "name";
  private static String INTERPOLATOR_NAME_KEY = "interpolatorName";
  private static String STRIP_KEY = "strip";

  // /CSON

  public void toFudgeMsg(final FudgeSerializationContext context, final MutableFudgeFieldContainer message) {
    context.objectToFudgeMsgWithClassHeaders(message, CURRENCY_KEY, null, _currency, Currency.class);
    message.add(NAME_KEY, _name);
    message.add(INTERPOLATOR_NAME_KEY, _interpolatorName);
    for (FixedIncomeStrip strip : _strips) {
      context.objectToFudgeMsgWithClassHeaders(message, STRIP_KEY, null, strip, FixedIncomeStrip.class);
    }
  }

  public static YieldCurveDefinition fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    final Currency currency = context.fieldValueToObject(Currency.class, message.getByName(CURRENCY_KEY));
    final String name = message.getString(NAME_KEY);
    final String interpolatorName = message.getString(INTERPOLATOR_NAME_KEY);
    final List<FudgeField> stripFields = message.getAllByName(STRIP_KEY);
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>(stripFields.size());
    for (FudgeField stripField : stripFields) {
      strips.add(context.fieldValueToObject(FixedIncomeStrip.class, stripField));
    }
    return new YieldCurveDefinition(currency, name, interpolatorName, strips);
  }

}
