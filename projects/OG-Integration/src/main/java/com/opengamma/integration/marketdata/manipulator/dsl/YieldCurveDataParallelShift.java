/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Map;
import java.util.Objects;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.analytics.ShiftType;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurveUtils;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveData;
import com.opengamma.id.ExternalIdBundle;

/**
 * {@link StructureManipulator} that shifts all points on a curve up or down by the same absolute amount.
 * TODO do we need to support relative and absolute shifts?
 */
public class YieldCurveDataParallelShift implements StructureManipulator<YieldCurveData> {

  private static final Logger s_logger = LoggerFactory.getLogger(YieldCurveDataParallelShift.class);

  /** Field name for Fudge message */
  private static final String SHIFT = "shift";
  /** The shift to apply  */
  private final double _shift;

  /* package */ YieldCurveDataParallelShift(double shift) {
    _shift = shift;
  }

  @Override
  public YieldCurveData execute(YieldCurveData curveData, ValueSpecification valueSpec) {
    s_logger.debug("Shifting curve data {} by {}", curveData.getCurveSpecification().getName(), _shift);
    Map<ExternalIdBundle, Double> dataPoints = curveData.getDataPoints();
    Map<ExternalIdBundle, Double> shiftedPoints = Maps.newHashMapWithExpectedSize(dataPoints.size());
    for (Map.Entry<ExternalIdBundle, Double> entry : dataPoints.entrySet()) {
      shiftedPoints.put(entry.getKey(), entry.getValue() + _shift);
    }
    return new YieldCurveData(curveData.getCurveSpecification(), shiftedPoints);
  }

  @Override
  public Class<YieldCurveData> getExpectedType() {
    return YieldCurveData.class;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, SHIFT, null, _shift);
    return msg;
  }

  public static YieldCurveDataParallelShift fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    Double shift = deserializer.fieldValueToObject(Double.class, msg.getByName(SHIFT));
    return new YieldCurveDataParallelShift(shift);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_shift);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final YieldCurveDataParallelShift other = (YieldCurveDataParallelShift) obj;
    return Objects.equals(this._shift, other._shift);
  }

  @Override
  public String toString() {
    return "YieldCurveParallelShift [" +
        "_shift=" + _shift +
        "]";
  }
}
