/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import javax.time.Instant;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;

/**
 * Mutable snapshot of yield curve data.
 */
public class ManageableYieldCurveSnapshot implements YieldCurveSnapshot {

  /**
   * The valuation instant.
   */
  private Instant _valuationTime;
  /**
   * The values.
   */
  private UnstructuredMarketDataSnapshot _values;

  /**
   * Gets the values.
   * 
   * @return the values
   */
  public UnstructuredMarketDataSnapshot getValues() {
    return _values;
  }

  /**
   * Sets the values.
   * @param values  the values
   */
  public void setValues(UnstructuredMarketDataSnapshot values) {
    _values = values;
  }

  /**
   * Gets the valuation instant.
   * 
   * @return the valuation instant
   */
  public Instant getValuationTime() {
    return _valuationTime;
  }

  /**
   * Sets the valuation instant.
   * 
   * @param valuationTime  the valuation instant
   */
  public void setValuationTime(Instant valuationTime) {
    _valuationTime = valuationTime;
  }

  public org.fudgemsg.FudgeMsg toFudgeMsg(FudgeSerializer serializer) {
    MutableFudgeMsg ret = serializer.newMessage();
    FudgeSerializer.addClassHeader(ret, ManageableYieldCurveSnapshot.class);
    serializer.addToMessage(ret, "values", null, _values);
    serializer.addToMessage(ret, "valuationTime", null, _valuationTime);
    return ret;
  }

  public static ManageableYieldCurveSnapshot fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    UnstructuredMarketDataSnapshot values = deserializer.fieldValueToObject(ManageableUnstructuredMarketDataSnapshot.class,
        msg.getByName("values"));
    Instant valuationTime = deserializer.fieldValueToObject(Instant.class, msg.getByName("valuationTime"));
    ManageableYieldCurveSnapshot ret = new ManageableYieldCurveSnapshot();

    ret.setValuationTime(valuationTime);
    ret.setValues(values);

    return ret;
  }

}
