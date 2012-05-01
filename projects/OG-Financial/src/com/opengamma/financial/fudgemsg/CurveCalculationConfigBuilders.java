/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.ArrayList;
import java.util.HashMap;
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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.calcconfig.CurveInstrumentConfig;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.id.UniqueIdentifiable;

/**
 * 
 */
/* package */ final class CurveCalculationConfigBuilders {

  private CurveCalculationConfigBuilders() {
  }

  @FudgeBuilderFor(MultiCurveCalculationConfig.class)
  public static final class MultiCurveCalculationConfigBuilder implements FudgeBuilder<MultiCurveCalculationConfig> {
    private static final UniqueIdentifiable[] EMPTY_UNIQUE_ID_ARRAY = new UniqueIdentifiable[0];
    private static final String CONFIG_NAME_FIELD = "configurationName";
    private static final String YIELD_CURVE_NAMES_FIELD = "yieldCurveNames";
    private static final String ID_FIELD = "ids";
    private static final String CALCULATION_METHOD_FIELD = "calculationMethods";
    private static final String INSTRUMENT_EXPOSURES_CURVE_NAME_FIELD = "instrumentExposureCurveName";
    private static final String INSTRUMENT_EXPOSURES_FOR_CURVE_FIELD = "instrumentExposuresForCurve";
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
      final Map<String, CurveInstrumentConfig> instrumentExposures = object.getCurveExposuresForInstruments();
      for (final Map.Entry<String, CurveInstrumentConfig> entry : instrumentExposures.entrySet()) {
        message.add(INSTRUMENT_EXPOSURES_CURVE_NAME_FIELD, entry.getKey());
        message.add(INSTRUMENT_EXPOSURES_FOR_CURVE_FIELD, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(entry.getValue()), entry.getValue().getClass()));
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
      final List<FudgeField> instrumentExposuresCurveNameField = message.getAllByName(INSTRUMENT_EXPOSURES_CURVE_NAME_FIELD);
      final List<FudgeField> instrumentExposuresForCurve = message.getAllByName(INSTRUMENT_EXPOSURES_FOR_CURVE_FIELD);
      if (instrumentExposuresCurveNameField.size() != instrumentExposuresForCurve.size()) {
        throw new OpenGammaRuntimeException("Should never happen");
      }
      final Map<String, CurveInstrumentConfig> curveInstrumentExposures = new HashMap<String, CurveInstrumentConfig>();
      for (int i = 0; i < instrumentExposuresCurveNameField.size(); i++) {
        final String curveName = deserializer.fieldValueToObject(String.class, instrumentExposuresCurveNameField.get(i));
        final CurveInstrumentConfig config = deserializer.fieldValueToObject(CurveInstrumentConfig.class, instrumentExposuresForCurve.get(i));
        curveInstrumentExposures.put(curveName, config);
      }
      if (message.hasField(EXOGENOUS_CURVES_FIELD)) {
        final List<FudgeField> exogenousConfigFields = message.getAllByName(EXOGENOUS_CURVES_FIELD);
        final List<String> exogenousConfigNames = new ArrayList<String>();
        for (final FudgeField field : exogenousConfigFields) {
          exogenousConfigNames.add(deserializer.fieldValueToObject(String.class, field));
        }
        return new MultiCurveCalculationConfig(calculationConfigName, yieldCurveNames.toArray(ArrayUtils.EMPTY_STRING_ARRAY),
            uniqueIds.toArray(EMPTY_UNIQUE_ID_ARRAY), calculationMethods.toArray(ArrayUtils.EMPTY_STRING_ARRAY), curveInstrumentExposures,
            exogenousConfigNames.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
      }
      return new MultiCurveCalculationConfig(calculationConfigName, yieldCurveNames.toArray(ArrayUtils.EMPTY_STRING_ARRAY),
          uniqueIds.toArray(EMPTY_UNIQUE_ID_ARRAY), calculationMethods.toArray(ArrayUtils.EMPTY_STRING_ARRAY), curveInstrumentExposures);
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
      final Map<StripInstrumentType, String[]> exposures = new HashMap<StripInstrumentType, String[]>();
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
