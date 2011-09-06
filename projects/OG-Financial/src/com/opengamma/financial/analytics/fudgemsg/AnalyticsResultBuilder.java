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
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

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
    private static final String CURVE_FIELD_NAME = "CurveName";
    private static final String PAIR_FIELD_NAME = "Pair";
    private static final String SENSITIVITIES_FIELD_NAME = "Sensitivities";

    @Override
    public PresentValueSensitivity buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final List<FudgeField> curveNameFields = message.getAllByName(CURVE_FIELD_NAME);
      final List<FudgeField> sensitivitiesFields = message.getAllByName(SENSITIVITIES_FIELD_NAME);
      final Map<String, List<DoublesPair>> data = new HashMap<String, List<DoublesPair>>();
      final int n = curveNameFields.size();
      if (sensitivitiesFields.size() != n) {
        throw new OpenGammaRuntimeException("Sensitivities list not same size as names list");
      }
      for (int i = 0; i < n; i++) {
        final FudgeField nameField = curveNameFields.get(i);
        final String name = deserializer.fieldValueToObject(String.class, nameField);
        final FudgeField listField = sensitivitiesFields.get(i);
        final List<DoublesPair> pairs = new ArrayList<DoublesPair>();
        final List<FudgeField> pairsField = ((FudgeMsg) listField.getValue()).getAllByName(PAIR_FIELD_NAME);
        for (final FudgeField pair : pairsField) {
          pairs.add((DoublesPair) deserializer.fieldValueToObject(Pair.class, pair));
        }
        data.put(name, pairs);
      }
      return new PresentValueSensitivity(data);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final PresentValueSensitivity object) {
      final Map<String, List<DoublesPair>> data = object.getSensitivities();
      for (final Map.Entry<String, List<DoublesPair>> entry : data.entrySet()) {
        message.add(CURVE_FIELD_NAME, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(entry.getKey()), entry.getKey().getClass()));
        final MutableFudgeMsg perPairMessage = serializer.newMessage();
        for (final DoublesPair pair : entry.getValue()) {
          perPairMessage.add(PAIR_FIELD_NAME, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(pair), pair.getClass()));
        }
        message.add(SENSITIVITIES_FIELD_NAME, null, perPairMessage);
      }
    }
  }
}
