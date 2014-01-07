/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.calcconfig.CurveInstrumentConfig;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;

/**
 * 
 */
/* package */final class CurveCalculationConfigBuilders {

  private CurveCalculationConfigBuilders() {
  }

  @FudgeBuilderFor(MultiCurveCalculationConfig.class)
  public static final class MultiCurveCalculationConfigBuilder implements FudgeBuilder<MultiCurveCalculationConfig> {
    private static final String CONFIG_NAME_FIELD = "configurationName";
    private static final String YIELD_CURVE_NAMES_FIELD = "yieldCurveNames";
    @Deprecated
    private static final String ID_FIELD = "ids";
    private static final String TARGET_FIELD = "target";
    private static final String CALCULATION_METHOD_FIELD = "calculationMethods";
    private static final String INSTRUMENT_EXPOSURES_CURVE_NAME_FIELD = "instrumentExposureCurveName";
    private static final String INSTRUMENT_EXPOSURES_FOR_CURVE_FIELD = "instrumentExposuresForCurve";
    private static final String EXOGENOUS_DATA_FIELD = "exogenousData";
    private static final String PER_CONFIG_FIELD = "exogenousCurveName";
    private static final String EXOGENOUS_CONFIG_FIELD = "exogenousCurveConfig";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final MultiCurveCalculationConfig object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(CONFIG_NAME_FIELD, object.getCalculationConfigName());
      message.add(CALCULATION_METHOD_FIELD, object.getCalculationMethod());
      serializer.addToMessage(message, TARGET_FIELD, null, object.getTarget());
      for (int i = 0; i < object.getYieldCurveNames().length; i++) {
        message.add(YIELD_CURVE_NAMES_FIELD, object.getYieldCurveNames()[i]);
      }
      final LinkedHashMap<String, CurveInstrumentConfig> instrumentExposures = object.getCurveExposuresForInstruments();
      if (instrumentExposures != null) {
        for (final Map.Entry<String, CurveInstrumentConfig> entry : instrumentExposures.entrySet()) {
          message.add(INSTRUMENT_EXPOSURES_CURVE_NAME_FIELD, entry.getKey());
          message.add(INSTRUMENT_EXPOSURES_FOR_CURVE_FIELD, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(entry.getValue()), entry.getValue().getClass()));
        }
      }
      if (object.getExogenousConfigData() != null) {
        for (final Map.Entry<String, String[]> entry : object.getExogenousConfigData().entrySet()) {
          message.add(EXOGENOUS_DATA_FIELD, entry.getKey());
          final MutableFudgeMsg perConfigMessage = serializer.newMessage();
          for (final String exogenousCurveName : entry.getValue()) {
            perConfigMessage.add(PER_CONFIG_FIELD, exogenousCurveName);
          }
          message.add(EXOGENOUS_CONFIG_FIELD, perConfigMessage);
        }
      }
      return message;
    }

    @Override
    public MultiCurveCalculationConfig buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String calculationConfigName = message.getString(CONFIG_NAME_FIELD);
      final String calculationMethod = message.getString(CALCULATION_METHOD_FIELD);
      final List<FudgeField> yieldCurveNamesFields = message.getAllByName(YIELD_CURVE_NAMES_FIELD);
      ComputationTargetSpecification target;
      try {
        target = (ComputationTargetSpecification) deserializer.fieldValueToObject(ComputationTargetReference.class, message.getByName(TARGET_FIELD));
      } catch (RuntimeException e) {
        // [PLAT-2286] Legacy support for UniqueIdentifiable member of the configuration
        final UniqueIdentifiable targetObject = deserializer.fieldValueToObject(UniqueIdentifiable.class, message.getByName(ID_FIELD));
        if (targetObject instanceof Currency) {
          target = ComputationTargetSpecification.of((Currency) targetObject);
        } else {
          target = ComputationTargetSpecification.of(targetObject.getUniqueId());
        }
      }
      final List<String> yieldCurveNames = new ArrayList<String>();
      for (int i = 0; i < yieldCurveNamesFields.size(); i++) {
        yieldCurveNames.add(deserializer.fieldValueToObject(String.class, yieldCurveNamesFields.get(i)));
      }
      final List<FudgeField> instrumentExposuresCurveNameField = message.getAllByName(INSTRUMENT_EXPOSURES_CURVE_NAME_FIELD);
      final List<FudgeField> instrumentExposuresForCurve = message.getAllByName(INSTRUMENT_EXPOSURES_FOR_CURVE_FIELD);
      if (instrumentExposuresCurveNameField.size() != instrumentExposuresForCurve.size()) {
        throw new OpenGammaRuntimeException("Should never happen");
      }
      LinkedHashMap<String, CurveInstrumentConfig> curveInstrumentExposures = null;
      if (message.hasField(INSTRUMENT_EXPOSURES_CURVE_NAME_FIELD)) {
        curveInstrumentExposures = new LinkedHashMap<String, CurveInstrumentConfig>();
        for (int i = 0; i < instrumentExposuresCurveNameField.size(); i++) {
          final String curveName = deserializer.fieldValueToObject(String.class, instrumentExposuresCurveNameField.get(i));
          final CurveInstrumentConfig config = deserializer.fieldValueToObject(CurveInstrumentConfig.class, instrumentExposuresForCurve.get(i));
          curveInstrumentExposures.put(curveName, config);
        }
      }
      if (message.hasField(EXOGENOUS_DATA_FIELD)) {
        final List<FudgeField> exogenousConfigFields = message.getAllByName(EXOGENOUS_DATA_FIELD);
        final List<FudgeField> exogenousCurveFields = message.getAllByName(EXOGENOUS_CONFIG_FIELD);
        if (exogenousConfigFields.size() != exogenousCurveFields.size()) {
          throw new OpenGammaRuntimeException("Should never happen");
        }
        final LinkedHashMap<String, String[]> exogenousConfig = new LinkedHashMap<String, String[]>();
        for (int i = 0; i < exogenousConfigFields.size(); i++) {
          final String configName = deserializer.fieldValueToObject(String.class, exogenousConfigFields.get(i));
          final List<FudgeField> curveNamesField = ((FudgeMsg) exogenousCurveFields.get(i).getValue()).getAllByName(PER_CONFIG_FIELD);
          final String[] curveNames = new String[curveNamesField.size()];
          int j = 0;
          for (final FudgeField field : curveNamesField) {
            curveNames[j++] = deserializer.fieldValueToObject(String.class, field);
          }
          exogenousConfig.put(configName, curveNames);
        }
        return new MultiCurveCalculationConfig(calculationConfigName, yieldCurveNames.toArray(ArrayUtils.EMPTY_STRING_ARRAY),
            target, calculationMethod, curveInstrumentExposures,
            exogenousConfig);
      }
      return new MultiCurveCalculationConfig(calculationConfigName, yieldCurveNames.toArray(ArrayUtils.EMPTY_STRING_ARRAY),
          target, calculationMethod, curveInstrumentExposures);
    }

  }

  @FudgeBuilderFor(CurveInstrumentConfig.class)
  public static final class CurveInstrumentConfigBuilder implements FudgeBuilder<CurveInstrumentConfig> {
    private static final String STRIP_INSTRUMENT_FIELD = "stripInstrumentType";
    private static final String PER_INSTRUMENT_FIELD_NAME = "curveName";
    private static final String CURVE_EXPOSURES_FIELD = "curveExposures";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurveInstrumentConfig object) {
      final MutableFudgeMsg message = serializer.newMessage();
      for (final Map.Entry<StripInstrumentType, String[]> entry : object.getExposures().entrySet()) {
        message.add(STRIP_INSTRUMENT_FIELD, entry.getKey().name());
        final MutableFudgeMsg perInstrumentMessage = serializer.newMessage();
        for (final String curveName : entry.getValue()) {
          perInstrumentMessage.add(PER_INSTRUMENT_FIELD_NAME, curveName);
        }
        message.add(CURVE_EXPOSURES_FIELD, null, perInstrumentMessage);
      }
      return message;
    }

    @Override
    public CurveInstrumentConfig buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final List<FudgeField> stripInstrumentTypeFields = message.getAllByName(STRIP_INSTRUMENT_FIELD);
      final List<FudgeField> curveExposureNameFields = message.getAllByName(CURVE_EXPOSURES_FIELD);
      if (stripInstrumentTypeFields.size() != curveExposureNameFields.size()) {
        throw new OpenGammaRuntimeException("Should never happen");
      }
      final Map<StripInstrumentType, String[]> exposures = Maps.newLinkedHashMap();
      for (int i = 0; i < stripInstrumentTypeFields.size(); i++) {
        final String stripName = deserializer.fieldValueToObject(String.class, stripInstrumentTypeFields.get(i));
        final List<FudgeField> namesField = ((FudgeMsg) curveExposureNameFields.get(i).getValue()).getAllByName(PER_INSTRUMENT_FIELD_NAME);
        final String[] curveNames = new String[namesField.size()];
        int j = 0;
        for (final FudgeField field : namesField) {
          curveNames[j++] = deserializer.fieldValueToObject(String.class, field);
        }
        exposures.put(StripInstrumentType.valueOf(stripName), curveNames);
      }
      return new CurveInstrumentConfig(exposures);
    }

  }
}
