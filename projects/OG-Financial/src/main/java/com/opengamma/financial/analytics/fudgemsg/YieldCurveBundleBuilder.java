/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.Maps;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.Currency;

/**
 * Fudge builder for {@code YieldCurveBundle}.
 */
@FudgeBuilderFor(YieldCurveBundle.class)
public final class YieldCurveBundleBuilder extends AbstractFudgeBuilder<YieldCurveBundle> {
  private static final String CURVES_FIELD_NAME = "curve";
  private static final String CURVES_NAME_FIELD_NAME = "curveName";
  private static final String CURRENCY_FIELD_NAME = "ccy";
  private static final String CURRENCY_CURVE_FIELD_NAME = "ccyCurve";
  private static final String FX_MATRIX_FIELD_NAME = "FXMatrix";


  @Override
  public YieldCurveBundle buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    // note name in curve & name in bundle may be different!
    final List<FudgeField> curveFields = message.getAllByName(CURVES_FIELD_NAME);
    final List<FudgeField> curveNameFields = message.getAllByName(CURVES_NAME_FIELD_NAME);
    final List<FudgeField> ccyFields = message.getAllByName(CURRENCY_FIELD_NAME);
    final List<FudgeField> ccyCurveFields = message.getAllByName(CURRENCY_CURVE_FIELD_NAME);
    final Map<String, YieldAndDiscountCurve> curves = new LinkedHashMap<>(curveFields.size());
    final Map<String, Currency> curveCurrencys = Maps.newHashMapWithExpectedSize(ccyFields.size());
    final FXMatrix fxMatrix = deserializer.fieldValueToObject(FXMatrix.class, message.getByName(FX_MATRIX_FIELD_NAME));
    for (int i = 0; i < curveFields.size(); i++) {
      final YieldAndDiscountCurve curve = deserializer.fieldValueToObject(YieldAndDiscountCurve.class, curveFields.get(i));
      final String name = deserializer.fieldValueToObject(String.class, curveNameFields.get(i));
      curves.put(name, curve);
    }
    for (int i = 0; i < ccyFields.size(); i++) {
      final String name = deserializer.fieldValueToObject(String.class, ccyCurveFields.get(i));
      final Currency ccy = deserializer.fieldValueToObject(Currency.class, ccyFields.get(i));
      curveCurrencys.put(name, ccy);
    }

    return new YieldCurveBundle(fxMatrix, curveCurrencys, curves);
  }

  @Override
  protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final YieldCurveBundle object) {
    serializer.addToMessageWithClassHeaders(message, FX_MATRIX_FIELD_NAME, null, object.getFxRates());
    for (final String curve : object.getAllNames()) {
      serializer.addToMessageWithClassHeaders(message, CURVES_FIELD_NAME, null, object.getCurve(curve));
      serializer.addToMessageWithClassHeaders(message, CURVES_NAME_FIELD_NAME, null, curve);
    }
    for (Map.Entry<String, Currency> ccyEntry : object.getCurrencyMap().entrySet()) {
      serializer.addToMessage(message, CURRENCY_CURVE_FIELD_NAME, null, ccyEntry.getKey());
      serializer.addToMessageWithClassHeaders(message, CURRENCY_FIELD_NAME, null, ccyEntry.getValue());
    }
  }
}
