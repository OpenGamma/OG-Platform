/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
/* package */final class AnalyticsResultBuilder {

  private AnalyticsResultBuilder() {
  }

  @FudgeBuilderFor(PresentValueSensitivity.class)
  public static final class PresentValueSensitivityBuilder extends AbstractFudgeBuilder<PresentValueSensitivity> {
    private static final String CURVE_NAME_LABEL = "curve name";
    private static final String SENSITIVITIES_LABEL = "sensitivities";
    private static final String PAIR_LABEL = null;

    @Override
    public PresentValueSensitivity buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
      final Map<String, List<DoublesPair>> data = new HashMap<String, List<DoublesPair>>();
      final List<FudgeField> curveNames = message.getAllByName(CURVE_NAME_LABEL);
      final List<FudgeField> sensitivities = message.getAllByName(SENSITIVITIES_LABEL);
      final Iterator<FudgeField> curveNamesIterator = curveNames.iterator();
      final Iterator<FudgeField> sensitivitiesIterator = sensitivities.iterator();
      while (curveNamesIterator.hasNext()) {
        final FudgeField curveName = curveNamesIterator.next();
        final FudgeField sensitivitiesList = sensitivitiesIterator.next();
        final String curveNameStr = context.fieldValueToObject(String.class, curveName);
        final FudgeMsg listSensitivities = (FudgeMsg) sensitivitiesList.getValue();
        final List<FudgeField> pairsFields = listSensitivities.getAllByName(PAIR_LABEL);
        final List<DoublesPair> results = new ArrayList<DoublesPair>();
        for (final FudgeField pairField : pairsFields) {
          final FudgeMsg pairMsg = (FudgeMsg) pairField.getValue();
          results.add(DoublesPair.of(pairMsg.getDouble(0).doubleValue(), pairMsg.getDouble(1).doubleValue()));
        }
        data.put(curveNameStr, results);
      }
      return new PresentValueSensitivity(data);
    }

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeMsg message, final PresentValueSensitivity object) {
      final Map<String, List<DoublesPair>> data = object.getSensitivities();
      final String[] curveNames = data.keySet().toArray(new String[0]);
      context.addToMessage(message, CURVE_NAME_LABEL, null, curveNames);
      for (final String curveName : curveNames) {
        final MutableFudgeMsg perCurveMessage = context.newMessage();
        final List<DoublesPair> sensitivities = data.get(curveName);
        for (final DoublesPair pair : sensitivities) {
          final MutableFudgeMsg perPairMessage = context.newMessage();
          perPairMessage.add(null, 0, pair.getFirst());
          perPairMessage.add(null, 1, pair.getSecond());
          perCurveMessage.add(PAIR_LABEL, null, perPairMessage);
        }
        message.add(SENSITIVITIES_LABEL, perCurveMessage);
      }
    }
  }

  //  @FudgeBuilderFor(PresentValueVolatilitySensitivityDataBundle.class)
  //  public static final class PresentValueVolatilitySensitivityDataBundleBuilder extends AbstractFudgeBuilder<PresentValueVolatilitySensitivityDataBundle> {
  //    private static final String CURRENCY_PAIR_LABEL = "currency pair";
  //    private static final String VEGA_LABEL = "vega label";
  //
  //    @Override
  //    public PresentValueVolatilitySensitivityDataBundle buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
  //      final Pair<Currency, Currency> currencyPair = null;
  //      final Map<DoublesPair, Double> vega = null;
  //      //      final PresentValueVolatilitySensitivityDataBundle result = new PresentValueVolatilitySensitivityDataBundle(currencyPair.getFirst(), currencyPair.getSecond());
  //      //      for (final Map.Entry<DoublesPair, Double> entry : vega.entrySet()) {
  //      //        result.add(entry.getKey(), entry.getValue());
  //      //      }
  //      //      return result;
  //    }
  //
  //    @Override
  //    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeMsg message, final PresentValueVolatilitySensitivityDataBundle object) {
  //      final Pair<Currency, Currency> currencyPair = object.getCurrencyPair();
  //      final Map<DoublesPair, Double> vega = object.getVega();
  //    }
  //
  //  }
}
