package com.opengamma.core.marketdatasnapshot;

import java.io.Serializable;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.util.money.Currency;

public class YieldCurveKey implements Serializable, Comparable<YieldCurveKey> {

  private final Currency _currency;
  private final String _name;

  /**
   * @param currency the currency
   * @param name the name
   */
  public YieldCurveKey(Currency currency, String name) {
    super();
    _currency = currency;
    _name = name;
  }

  /**
   * Gets the currency field.
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the name field.
   * @return the name
   */
  public String getName() {
    return _name;
  }

  
  public MutableFudgeMsg toFudgeMsg(final FudgeSerializationContext context) {
    
    final MutableFudgeMsg msg = context.newMessage();
    msg.add("currency", _currency.getCode());
    msg.add("name", _name);
    
    return msg;
  }

  public static YieldCurveKey fromFudgeMsg(final FudgeDeserializationContext context, final FudgeMsg msg) {
    return new YieldCurveKey(Currency.of(msg.getString("currency")), msg.getString("name"));
  }
  
  @Override
  public int compareTo(YieldCurveKey o) {
    int currCompare = _currency.compareTo(o.getCurrency());
    if (currCompare != 0) {
      return currCompare;
    }
    return _name.compareTo(o.getName());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_currency == null) ? 0 : _currency.hashCode());
    result = prime * result + ((_name == null) ? 0 : _name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    YieldCurveKey other = (YieldCurveKey) obj;
    if (_currency == null) {
      if (other._currency != null)
        return false;
    } else if (!_currency.equals(other._currency))
      return false;
    if (_name == null) {
      if (other._name != null)
        return false;
    } else if (!_name.equals(other._name))
      return false;
    return true;
  }

}
