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

import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.fixedincome.YieldCurveNodeSensitivityDataBundle;
import com.opengamma.util.money.Currency;

/**
 * 
 */
@FudgeBuilderFor(YieldCurveNodeSensitivityDataBundle.class)
public class YieldCurveNodeSensitivityDataBundleBuilder extends AbstractFudgeBuilder<YieldCurveNodeSensitivityDataBundle> {

  /** Field name. */
  public static final String MATRIX_FIELD_NAME = "Matrix";
  /** Field name. */
  public static final String CURRENCY_FIELD_NAME = "Currency";
  /** Field name. */
  public static final String CURVE_FIELD_NAME = "CurveName";

  @Override
  protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final YieldCurveNodeSensitivityDataBundle object) {
    serializer.addToMessage(message, CURRENCY_FIELD_NAME, null, object.getCurrency());
    serializer.addToMessage(message, MATRIX_FIELD_NAME, null, object.getLabelledMatrix());
    serializer.addToMessage(message, CURVE_FIELD_NAME, null, object.getYieldCurveName());
  }

  @Override
  public YieldCurveNodeSensitivityDataBundle buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final Currency ccy = deserializer.fieldValueToObject(Currency.class, message.getByName(CURRENCY_FIELD_NAME));
    final DoubleLabelledMatrix1D labelledMatrix = deserializer.fieldValueToObject(DoubleLabelledMatrix1D.class, message.getByName(MATRIX_FIELD_NAME));
    final String curveName = deserializer.fieldValueToObject(String.class, message.getByName(CURVE_FIELD_NAME));
    return new YieldCurveNodeSensitivityDataBundle(ccy, labelledMatrix, curveName);
  }

}
