/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import java.io.Serializable;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Instant;

import com.opengamma.core.marketdatasnapshot.CurveSnapshot;

/**
 * Mutable snapshot of curve data.
 */
public class ManageableCurveSnapshot implements CurveSnapshot, Serializable {

  /**
   * The valuation instant.
   */
  private Instant _valuationTime;
  /**
   * The values.
   */
  private ManageableUnstructuredMarketDataSnapshot _values;

  /**
   * Gets the values.
   *
   * @return the values
   */
  @Override
  public ManageableUnstructuredMarketDataSnapshot getValues() {
    return _values;
  }

  /**
   * Sets the values.
   *
   * @param values the values
   */
  public void setValues(final ManageableUnstructuredMarketDataSnapshot values) {
    _values = values;
  }

  /**
   * Gets the valuation instant.
   *
   * @return the valuation instant
   */
  @Override
  public Instant getValuationTime() {
    return _valuationTime;
  }

  /**
   * Sets the valuation instant.
   *
   * @param valuationTime the valuation instant
   */
  public void setValuationTime(final Instant valuationTime) {
    _valuationTime = valuationTime;
  }

  public FudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg ret = serializer.newMessage();
    // TODO: this should not be adding its own class header; the caller should add it based on application knowledge about the receiving end
    FudgeSerializer.addClassHeader(ret, ManageableCurveSnapshot.class);
    serializer.addToMessage(ret, "values", null, _values);
    serializer.addToMessage(ret, "valuationTime", null, _valuationTime);
    return ret;
  }

  public static ManageableCurveSnapshot fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final ManageableCurveSnapshot ret = new ManageableCurveSnapshot();
    FudgeField field = msg.getByName("values");
    if (field != null) {
      ret.setValues(deserializer.fieldValueToObject(ManageableUnstructuredMarketDataSnapshot.class, field));
    }
    field = msg.getByName("valuationTime");
    if (field != null) {
      ret.setValuationTime(deserializer.fieldValueToObject(Instant.class, field));
    }
    return ret;
  }
}
