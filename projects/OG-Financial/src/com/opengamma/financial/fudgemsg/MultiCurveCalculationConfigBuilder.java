/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.id.UniqueIdentifiable;

/**
 * 
 */
@FudgeBuilderFor(MultiCurveCalculationConfig.class)
public class MultiCurveCalculationConfigBuilder implements FudgeBuilder<MultiCurveCalculationConfig> {
  private static final UniqueIdentifiable[] EMPTY_UNIQUE_ID_ARRAY = new UniqueIdentifiable[0];
  private static final String CONFIG_NAME_FIELD = "configurationName";
  private static final String YIELD_CURVE_NAMES_FIELD = "yieldCurveNames";
  private static final String ID_FIELD = "ids";
  private static final String CALCULATION_METHOD_FIELD = "calculationMethods";
  private static final String EXOGENOUS_CURVES_FIELD = "exogenousCurveConfigurationNames";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final MultiCurveCalculationConfig object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(CONFIG_NAME_FIELD, object.getCalculationConfigName());
    for (int i = 0; i < object.getYieldCurveNames().length; i++) {
      message.add(YIELD_CURVE_NAMES_FIELD, object.getYieldCurveNames()[i]);
      final UniqueIdentifiable uniqueIdentifiable = object.getUniqueIds()[i];
      message.add(ID_FIELD, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(uniqueIdentifiable), uniqueIdentifiable.getClass()));
      message.add(CALCULATION_METHOD_FIELD, object.getCalculationMethods()[i]);
    }
    if (object.getExogenousConfigNames() != null) {
      for (final String name : object.getExogenousConfigNames()) {
        message.add(EXOGENOUS_CURVES_FIELD, name);
      }
    }
    return message;
  }

  @Override
  public MultiCurveCalculationConfig buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String calculationConfigName = message.getString(CONFIG_NAME_FIELD);
    final List<FudgeField> yieldCurveNamesFields = message.getAllByName(YIELD_CURVE_NAMES_FIELD);
    final List<FudgeField> idFields = message.getAllByName(ID_FIELD);
    final List<FudgeField> calculationMethodFields = message.getAllByName(CALCULATION_METHOD_FIELD);
    final List<String> yieldCurveNames = new ArrayList<String>();
    final List<UniqueIdentifiable> uniqueIds = new ArrayList<UniqueIdentifiable>();
    final List<String> calculationMethods = new ArrayList<String>();
    for (int i = 0; i < yieldCurveNamesFields.size(); i++) {
      yieldCurveNames.add(deserializer.fieldValueToObject(String.class, yieldCurveNamesFields.get(i)));
      uniqueIds.add(deserializer.fieldValueToObject(UniqueIdentifiable.class, idFields.get(i)));
      calculationMethods.add(deserializer.fieldValueToObject(String.class, calculationMethodFields.get(i)));
    }
    if (message.hasField(EXOGENOUS_CURVES_FIELD)) {
      final List<FudgeField> exogenousConfigFields = message.getAllByName(EXOGENOUS_CURVES_FIELD);
      final List<String> exogenousConfigNames = new ArrayList<String>();
      for (final FudgeField field : exogenousConfigFields) {
        exogenousConfigNames.add(deserializer.fieldValueToObject(String.class, field));
      }
      return new MultiCurveCalculationConfig(calculationConfigName, yieldCurveNames.toArray(ArrayUtils.EMPTY_STRING_ARRAY),
          uniqueIds.toArray(EMPTY_UNIQUE_ID_ARRAY), calculationMethods.toArray(ArrayUtils.EMPTY_STRING_ARRAY), exogenousConfigNames.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
    }
    return new MultiCurveCalculationConfig(calculationConfigName, yieldCurveNames.toArray(ArrayUtils.EMPTY_STRING_ARRAY),
        uniqueIds.toArray(EMPTY_UNIQUE_ID_ARRAY), calculationMethods.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
  }

}
