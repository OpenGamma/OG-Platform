/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.io.Serializable;

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
public class VolatilitySurfaceKey implements StructuredMarketDataKey, Comparable<VolatilitySurfaceKey>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

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
   * The quite units.
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
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(instrumentType, "instrumentType");
    ArgumentChecker.notNull(quoteType, "quoteType");
    ArgumentChecker.notNull(quoteUnits, "quoteUnits");
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
    int i = _target.compareTo(other.getTarget());
    if (i != 0) {
      return i;
    }
    i = _name.compareTo(other.getName());
    if (i != 0) {
      return i;
    }
    i = _instrumentType.compareTo(other._instrumentType);
    if (i != 0) {
      return i;
    }
    i = _quoteType.compareTo(other._quoteType);
    if (i != 0) {
      return i;
    }
    return _quoteUnits.compareTo(other._quoteUnits);
  }

  /**
   * Checks if this key equals another.
   * <p>
   * This checks the currency and name.
   * 
   * @param object  the object to compare to, null returns false
   * @return true if equal
   */
  @Override
  public boolean equals(Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof VolatilitySurfaceKey) {
      final VolatilitySurfaceKey other = (VolatilitySurfaceKey) object;
      return _target.equals(other._target)
          && _name.equals(other._name)
          && _instrumentType.equals(other._instrumentType)
          && _quoteType.equals(other._quoteType)
          && _quoteUnits.equals(other._quoteUnits);
    }
    return false;
  }

  /**
   * Returns a suitable hash code.
   * 
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return _target.hashCode() ^ _name.hashCode() ^ _instrumentType.hashCode() ^ _quoteType.hashCode();
  }

  //-------------------------------------------------------------------------
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
