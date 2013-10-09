/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A key used to identify a volatility surface.
 * <p>
 * This class is immutable and thread-safe.
 */
public class VolatilitySurfaceKey extends StructuredMarketDataKey implements Comparable<VolatilitySurfaceKey> {

  /** Serialization version. */
  private static final long serialVersionUID = 3L;

  /**
   * The target.
   */
  private final UniqueId _target;
  /**
   * The curve name.
   */
  private final String _name;
  /**
   * The instrument type.
   */
  private final String _instrumentType;
  /**
   * The quote type.
   */
  private final String _quoteType;
  /**
   * The quote units.
   */
  private final String _quoteUnits;

  /**
   * Creates an instance.
   * 
   * @param target  the target
   * @param name  the name
   * @param instrumentType  the instrument type
   * @param quoteType the quote type
   * @param quoteUnits the quote units
   */
  public VolatilitySurfaceKey(final UniqueIdentifiable target, final String name, final String instrumentType, final String quoteType, final String quoteUnits) {
    ArgumentChecker.notNull(target, "target");
    _target = target.getUniqueId();
    _name = name;
    _instrumentType = instrumentType;
    _quoteType = quoteType;
    _quoteUnits = quoteUnits;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the target field.
   * 
   * @return the target
   */
  public UniqueId getTarget() {
    return _target;
  }

  /**
   * Gets the name field.
   * 
   * @return the name
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets the instrumentType field.
   * @return the instrumentType
   */
  public String getInstrumentType() {
    return _instrumentType;
  }

  /**
   * Gets the quote type field.
   * @return the quote type
   */
  public String getQuoteType() {
    return _quoteType;
  }

  /**
   * Gets the quote units field
   * @return the quote units
   */
  public String getQuoteUnits() {
    return _quoteUnits;
  }

  //-------------------------------------------------------------------------
  /**
   * Compares this key to another, by currency then name.
   * 
   * @param other  the other key, not null
   * @return the comparison value
   */
  @Override
  public int compareTo(VolatilitySurfaceKey other) {
    if (other == null) {
      throw new NullPointerException();
    }
    int i = _target.compareTo(other.getTarget());
    if (i != 0) {
      return i;
    }
    i = ObjectUtils.compare(_name, other._name);
    if (i != 0) {
      return i;
    }
    i = ObjectUtils.compare(_instrumentType, other._instrumentType);
    if (i != 0) {
      return i;
    }
    i = ObjectUtils.compare(_quoteType, other._quoteType);
    if (i != 0) {
      return i;
    }
    return ObjectUtils.compare(_quoteUnits, other._quoteUnits);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VolatilitySurfaceKey that = (VolatilitySurfaceKey) o;

    if (_instrumentType != null ? !_instrumentType.equals(that._instrumentType) : that._instrumentType != null) {
      return false;
    }
    if (_name != null ? !_name.equals(that._name) : that._name != null) {
      return false;
    }
    if (_quoteType != null ? !_quoteType.equals(that._quoteType) : that._quoteType != null) {
      return false;
    }
    if (_quoteUnits != null ? !_quoteUnits.equals(that._quoteUnits) : that._quoteUnits != null) {
      return false;
    }
    if (!_target.equals(that._target)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = _target.hashCode();
    result = 31 * result + (_name != null ? _name.hashCode() : 0);
    result = 31 * result + (_instrumentType != null ? _instrumentType.hashCode() : 0);
    result = 31 * result + (_quoteType != null ? _quoteType.hashCode() : 0);
    result = 31 * result + (_quoteUnits != null ? _quoteUnits.hashCode() : 0);
    return result;
  }

  @Override
  public <T> T accept(final Visitor<T> visitor) {
    return visitor.visitVolatilitySurfaceKey(this);
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add("target", _target.toString());
    msg.add("name", _name);
    msg.add("instrumentType", _instrumentType);
    msg.add("quoteType", _quoteType);
    msg.add("quoteUnits", _quoteUnits);
    return msg;
  }

  public static VolatilitySurfaceKey fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final UniqueId targetUid;
    String target = msg.getString("target");
    if (target == null) {
      //Handle old form of snapshot
      Currency curr = Currency.of(msg.getString("currency"));
      targetUid = curr.getUniqueId();
    } else {
      targetUid = UniqueId.parse(target);
    }
    return new VolatilitySurfaceKey(targetUid, msg.getString("name"), msg.getString("instrumentType"), msg.getString("quoteType"), msg.getString("quoteUnits"));
  }

}
