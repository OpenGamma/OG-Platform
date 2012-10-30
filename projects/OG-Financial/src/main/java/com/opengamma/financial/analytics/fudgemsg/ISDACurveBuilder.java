/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.math.curve.DoublesCurve;

/**
 * Fudge builder for ISDA curve
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 * @see ISDACurve
 */
@FudgeBuilderFor(ISDACurve.class)
public class ISDACurveBuilder extends AbstractFudgeBuilder<ISDACurve> {
  
  private static final String NAME_FIELD_NAME = "name";
  private static final String CURVE_FIELD_NAME = "curve";
  private static final String OFFSET_FIELD_NAME = "offset";
  private static final String ZERO_DISCOUNT_FACTOR_FIELD_NAME = "zero discount factor";
  private static final String SHIFTED_TIME_POINTS_FIELD_NAME = "shifted time points";

  @Override
  public ISDACurve buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    
    final DoublesCurve curve = (DoublesCurve) deserializer.fieldValueToObject(message.getByName(CURVE_FIELD_NAME));
    
    final String name;
    if (message.hasField(NAME_FIELD_NAME)) {
      name = message.getString(NAME_FIELD_NAME);
    } else {
      name = curve.getName();
    }
    
    final double offset = deserializer.fieldValueToObject(double.class, message.getByName(OFFSET_FIELD_NAME)).doubleValue();
    final double zeroDiscountFactor = deserializer.fieldValueToObject(double.class, message.getByName(ZERO_DISCOUNT_FACTOR_FIELD_NAME)).doubleValue();
    final double[] shiftedTimePoints = deserializer.fieldValueToObject(double[].class, message.getByName(SHIFTED_TIME_POINTS_FIELD_NAME));
    
    return new ISDACurve(name, curve, offset, zeroDiscountFactor, shiftedTimePoints);
  }

  @Override
  protected void buildMessage(FudgeSerializer serializer, MutableFudgeMsg message, ISDACurve object) {
    serializer.addToMessageWithClassHeaders(message, NAME_FIELD_NAME, null, object.getName(), String.class);
    serializer.addToMessageWithClassHeaders(message, CURVE_FIELD_NAME, null, object.getCurve());
    serializer.addToMessageWithClassHeaders(message, OFFSET_FIELD_NAME, null, object.getOffset(), double.class);
    serializer.addToMessageWithClassHeaders(message, ZERO_DISCOUNT_FACTOR_FIELD_NAME, null, object.getZeroDiscountFactor(), double.class);
    serializer.addToMessageWithClassHeaders(message, SHIFTED_TIME_POINTS_FIELD_NAME, null, object.getTimePoints(), double[].class);
  }
  
}
