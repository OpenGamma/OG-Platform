/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
/* package */final class AnalyticsResultBuilder {

  private AnalyticsResultBuilder() {
  }

  @FudgeBuilderFor(PresentValueSensitivity.class)
  public static final class PresentValueSensitivityBuilder extends AbstractFudgeBuilder<PresentValueSensitivity> {
    private static final String CURVE_NAME = "CurveName";
    private static final String PAIR_NAME = "Pair";
    private static final String SENSITIVITIES_NAME = "Sensitivities";

    @Override
    public PresentValueSensitivity buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
      final List<FudgeField> curveNameFields = message.getAllByName(CURVE_NAME);
      final List<FudgeField> sensitivitiesFields = message.getAllByName(SENSITIVITIES_NAME);
      final Map<String, List<DoublesPair>> data = new HashMap<String, List<DoublesPair>>();
      final int n = curveNameFields.size();
      if (sensitivitiesFields.size() != n) {
        throw new OpenGammaRuntimeException("Sensitivities list not same size as names list");
      }
      for (int i = 0; i < n; i++) {
        final FudgeField nameField = curveNameFields.get(i);
        final String name = context.fieldValueToObject(String.class, nameField);
        final FudgeField listField = sensitivitiesFields.get(i);
        final List<DoublesPair> pairs = new ArrayList<DoublesPair>();
        final List<FudgeField> pairsField = ((FudgeMsg) listField.getValue()).getAllByName(PAIR_NAME);
        for (final FudgeField pair : pairsField) {
          pairs.add((DoublesPair) context.fieldValueToObject(Pair.class, pair));
        }
        data.put(name, pairs);
      }
      return new PresentValueSensitivity(data);
    }

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeMsg message, final PresentValueSensitivity object) {
      final Map<String, List<DoublesPair>> data = object.getSensitivities();
      for (final Map.Entry<String, List<DoublesPair>> entry : data.entrySet()) {
        message.add(CURVE_NAME, null, FudgeSerializationContext.addClassHeader(context.objectToFudgeMsg(entry.getKey()), entry.getKey().getClass()));
        final MutableFudgeMsg perPairMessage = context.newMessage();
        for (final DoublesPair pair : entry.getValue()) {
          perPairMessage.add(PAIR_NAME, null, FudgeSerializationContext.addClassHeader(context.objectToFudgeMsg(pair), pair.getClass()));
        }
        message.add(SENSITIVITIES_NAME, null, perPairMessage);
      }
    }
  }
}
