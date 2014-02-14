/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.AnnuallyCompoundedForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Contains results of calculations associated with curves
 */
/* package */final class CurveResultBuilders {

  private CurveResultBuilders() {
  }

  /**
   * Fudge builder for {@link SimplyCompoundedForwardSensitivity}
   */
  @FudgeBuilderFor(SimplyCompoundedForwardSensitivity.class)
  public static final class SimplyCompoundedForwardSensitivityBuilder extends AbstractFudgeBuilder<SimplyCompoundedForwardSensitivity> {
    /** The start time field */
    private static final String START_TIME = "startTime";
    /** The end time field */
    private static final String END_TIME = "endTime";
    /** The accrual factor */
    private static final String ACCRUAL_FACTOR = "accrualFactor";
    /** The value */
    private static final String VALUE = "value";

    @Override
    public SimplyCompoundedForwardSensitivity buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double startTime = message.getDouble(START_TIME);
      final double endTime = message.getDouble(END_TIME);
      final double accrualFactor = message.getDouble(ACCRUAL_FACTOR);
      final double value = message.getDouble(VALUE);
      return new SimplyCompoundedForwardSensitivity(startTime, endTime, accrualFactor, value);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final SimplyCompoundedForwardSensitivity object) {
      message.add(START_TIME, object.getStartTime());
      message.add(END_TIME, object.getEndTime());
      message.add(ACCRUAL_FACTOR, object.getAccrualFactor());
      message.add(VALUE, object.getValue());
    }

  }

  /**
   * Fudge builder for {@link AnnuallyCompoundedForwardSensitivity}
   */
  @FudgeBuilderFor(AnnuallyCompoundedForwardSensitivity.class)
  public static final class AnnuallyCompoundedForwardSensitivityBuilder extends AbstractFudgeBuilder<AnnuallyCompoundedForwardSensitivity> {
    /** The start time field */
    private static final String START_TIME = "startTime";
    /** The end time field */
    private static final String END_TIME = "endTime";
    /** The accrual factor */
    private static final String ACCRUAL_FACTOR = "accrualFactor";
    /** The value */
    private static final String VALUE = "value";

    @Override
    public AnnuallyCompoundedForwardSensitivity buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double startTime = message.getDouble(START_TIME);
      final double endTime = message.getDouble(END_TIME);
      final double accrualFactor = message.getDouble(ACCRUAL_FACTOR);
      final double value = message.getDouble(VALUE);
      return new AnnuallyCompoundedForwardSensitivity(startTime, endTime, accrualFactor, value);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final AnnuallyCompoundedForwardSensitivity object) {
      message.add(START_TIME, object.getStartTime());
      message.add(END_TIME, object.getEndTime());
      message.add(ACCRUAL_FACTOR, object.getAccrualFactor());
      message.add(VALUE, object.getValue());
    }

  }

  /**
   * Fudge builder for {@link MulticurveSensitivity}
   */
  @FudgeBuilderFor(MulticurveSensitivity.class)
  public static final class MulticurveSensitivityBuilder extends AbstractFudgeBuilder<MulticurveSensitivity> {
    /** The yield curve name field */
    private static final String YIELD_CURVE_NAME = "yieldCurveName";
    /** Field for the map containing the sensitivities to yield curves */
    private static final String SENSITIVITY_TO_YIELD_DATA = "allSensitivityToYieldData";
    /** Field for the cash flow times of the sensitivities to a particular yield curve */
    private static final String SENSITIVITY_TO_YIELD_TIME = "cashFlowTimeForYield";
    /** Field for the sensitivity to a particular yield curve at a given time */
    private static final String SENSITIVITY_TO_YIELD_VALUE = "sensitivityForYield";
    /** The forward curve name field */
    private static final String FORWARD_CURVE_NAME = "forwardCurveName";
    /** Field for the map containing the sensitivities to the forward curve */
    private static final String SENSITIVITY_TO_FORWARD_DATA = "allSensitivityToForwardData";
    /** Field for the sensitivity to a particular forward curve at a given time */
    private static final String SENSITIVITY_TO_FORWARD_VALUE = "sensitivityForForward";

    @Override
    public MulticurveSensitivity buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Map<String, List<DoublesPair>> yieldCurveSensitivities = new HashMap<>();
      final Map<String, List<ForwardSensitivity>> forwardCurveSensitivities = new HashMap<>();
      final List<FudgeField> yieldCurveFields = message.getAllByName(SENSITIVITY_TO_YIELD_DATA);
      for (final FudgeField yieldCurveField : yieldCurveFields) {
        final FudgeMsg perCurveMessage = (FudgeMsg) yieldCurveField.getValue();
        final String yieldCurveName = perCurveMessage.getString(YIELD_CURVE_NAME);
        final List<FudgeField> timeFields = perCurveMessage.getAllByName(SENSITIVITY_TO_YIELD_TIME);
        final List<FudgeField> valueFields = perCurveMessage.getAllByName(SENSITIVITY_TO_YIELD_VALUE);
        if (timeFields.size() != valueFields.size()) {
          throw new OpenGammaRuntimeException("number of times and values not equal");
        }
        final List<DoublesPair> sensitivities = new ArrayList<>();
        for (int i = 0; i < timeFields.size(); i++) {
          final Double time = deserializer.fieldValueToObject(Double.class, timeFields.get(i));
          final Double sensitivity = deserializer.fieldValueToObject(Double.class, valueFields.get(i));
          sensitivities.add(DoublesPair.of(time.doubleValue(), sensitivity.doubleValue()));
        }
        yieldCurveSensitivities.put(yieldCurveName, sensitivities);
      }
      final List<FudgeField> forwardCurveFields = message.getAllByName(SENSITIVITY_TO_FORWARD_DATA);
      for (final FudgeField forwardCurveField : forwardCurveFields) {
        final FudgeMsg perCurveMessage = (FudgeMsg) forwardCurveField.getValue();
        final String forwardCurveName = perCurveMessage.getString(FORWARD_CURVE_NAME);
        final List<FudgeField> valueFields = perCurveMessage.getAllByName(SENSITIVITY_TO_FORWARD_VALUE);
        final List<ForwardSensitivity> sensitivities = new ArrayList<>();
        for (int i = 0; i < valueFields.size(); i++) {
          final ForwardSensitivity sensitivity = deserializer.fieldValueToObject(ForwardSensitivity.class, valueFields.get(i));
          sensitivities.add(sensitivity);
        }
        forwardCurveSensitivities.put(forwardCurveName, sensitivities);
      }
      return MulticurveSensitivity.of(yieldCurveSensitivities, forwardCurveSensitivities);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final MulticurveSensitivity object) {
      final Map<String, List<DoublesPair>> yieldCurveSensitivities = object.getYieldDiscountingSensitivities();
      final Map<String, List<ForwardSensitivity>> forwardSensitivities = object.getForwardSensitivities();
      for (final Map.Entry<String, List<DoublesPair>> entry : yieldCurveSensitivities.entrySet()) {
        final MutableFudgeMsg perCurveMessage = serializer.newMessage();
        perCurveMessage.add(YIELD_CURVE_NAME, entry.getKey());
        for (final DoublesPair pair : entry.getValue()) {
          perCurveMessage.add(SENSITIVITY_TO_YIELD_TIME, pair.first);
          perCurveMessage.add(SENSITIVITY_TO_YIELD_VALUE, pair.second);
        }
        message.add(SENSITIVITY_TO_YIELD_DATA, perCurveMessage);
      }
      for (final Map.Entry<String, List<ForwardSensitivity>> entry : forwardSensitivities.entrySet()) {
        final MutableFudgeMsg perCurveMessage = serializer.newMessage();
        perCurveMessage.add(FORWARD_CURVE_NAME, entry.getKey());
        for (final ForwardSensitivity sensitivity : entry.getValue()) {
          serializer.addToMessageWithClassHeaders(perCurveMessage, SENSITIVITY_TO_FORWARD_VALUE, null, sensitivity);
        }
        message.add(SENSITIVITY_TO_FORWARD_DATA, perCurveMessage);
      }
    }

  }

  /**
   * Fudge builder for {@link MultipleCurrencyMulticurveSensitivity}
   */
  @FudgeBuilderFor(MultipleCurrencyMulticurveSensitivity.class)
  public static final class MultipleCurrencyMulticurveSensitivityBuilder extends AbstractFudgeBuilder<MultipleCurrencyMulticurveSensitivity> {
    /** The currencies field */
    private static final String CURRENCY = "currency";
    /** The sensitivities field */
    private static final String SENSITIVITIES = "sensitivities";

    @Override
    public MultipleCurrencyMulticurveSensitivity buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final List<FudgeField> currencies = message.getAllByName(CURRENCY);
      final List<FudgeField> sensitivities = message.getAllByName(SENSITIVITIES);
      if (currencies.size() != sensitivities.size()) {
        throw new OpenGammaRuntimeException("Should have same number of sensitivities as currencies");
      }
      MultipleCurrencyMulticurveSensitivity result = new MultipleCurrencyMulticurveSensitivity();
      for (int i = 0; i < currencies.size(); i++) {
        final Currency currency = Currency.of((String) currencies.get(i).getValue());
        final MulticurveSensitivity sensitivity = deserializer.fieldValueToObject(MulticurveSensitivity.class, sensitivities.get(i));
        result = result.plus(currency, sensitivity);
      }
      return result;
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final MultipleCurrencyMulticurveSensitivity object) {
      final Map<Currency, MulticurveSensitivity> sensitivities = object.getSensitivities();
      for (final Map.Entry<Currency, MulticurveSensitivity> entry : sensitivities.entrySet()) {
        message.add(CURRENCY, entry.getKey().getCode());
        serializer.addToMessageWithClassHeaders(message, SENSITIVITIES, null, entry.getValue());
      }
    }

  }

  /**
   * Fudge builder for {@link SimpleParameterSensitivity}
   */
  @FudgeBuilderFor(SimpleParameterSensitivity.class)
  public static final class SimpleParameterSensitivityBuilder extends AbstractFudgeBuilder<SimpleParameterSensitivity> {
    /** The curve name field */
    private static final String CURVE_NAME = "curveName";
    /** The sensitivity field */
    private static final String SENSITIVITY = "sensitivity";
    /** The sensitivity vector per curve field */
    private static final String SENSITIVITIES_FOR_CURVE = "sensitivitiesForCurve";

    @Override
    public SimpleParameterSensitivity buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final LinkedHashMap<String, DoubleMatrix1D> sensitivities = new LinkedHashMap<>();
      final List<FudgeField> curves = message.getAllByName(CURVE_NAME);
      final List<FudgeField> sensitivitiesPerCurve = message.getAllByName(SENSITIVITIES_FOR_CURVE);
      if (curves.size() != sensitivitiesPerCurve.size()) {
        throw new OpenGammaRuntimeException("Should have a vector of sensitivities for each curve name");
      }
      for (int i = 0; i < curves.size(); i++) {
        final String curve = (String) curves.get(i).getValue();
        final FudgeMsg perCurveMessage = (FudgeMsg) sensitivitiesPerCurve.get(i).getValue();
        final List<FudgeField> perCurveFields = perCurveMessage.getAllByName(SENSITIVITY);
        final double[] values = new double[perCurveFields.size()];
        for (int j = 0; j < perCurveFields.size(); j++) {
          values[j] = (Double) perCurveFields.get(j).getValue();
        }
        sensitivities.put(curve, new DoubleMatrix1D(values));
      }
      return new SimpleParameterSensitivity(sensitivities);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final SimpleParameterSensitivity object) {
      final Map<String, DoubleMatrix1D> sensitivities = object.getSensitivities();
      for (final Map.Entry<String, DoubleMatrix1D> entry : sensitivities.entrySet()) {
        final MutableFudgeMsg perCurveMessage = serializer.newMessage();
        message.add(CURVE_NAME, entry.getKey());
        final double[] sensitivity = entry.getValue().getData();
        for (final double d : sensitivity) {
          perCurveMessage.add(SENSITIVITY, d);
        }
        message.add(SENSITIVITIES_FOR_CURVE, perCurveMessage);
      }
    }

  }

  /**
   * Fudge builder for {@link MultipleCurrencyParameterSensitivity}
   */
  @FudgeBuilderFor(MultipleCurrencyParameterSensitivity.class)
  public static final class MultipleCurrencyParameterSensitivityBuilder extends AbstractFudgeBuilder<MultipleCurrencyParameterSensitivity> {
    /** The curve name field */
    private static final String CURVE_NAME = "curveName";
    /** The currency field */
    private static final String CURRENCY = "currency";
    /** The sensitivities for a curve / currency pair field */
    private static final String SENSITIVITIES_FOR_PAIR = "sensitivitiesForPair";
    /** The sensitivities field */
    private static final String SENSITIVITY = "sensitivity";

    @Override
    public MultipleCurrencyParameterSensitivity buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivities = new LinkedHashMap<>();
      final List<FudgeField> curves = message.getAllByName(CURVE_NAME);
      final List<FudgeField> currencies = message.getAllByName(CURRENCY);
      if (curves.size() != currencies.size()) {
        throw new OpenGammaRuntimeException("Should have a currency for each curve name");
      }
      final List<FudgeField> sensitivitiesPerCurve = message.getAllByName(SENSITIVITIES_FOR_PAIR);
      if (curves.size() != sensitivitiesPerCurve.size()) {
        throw new OpenGammaRuntimeException("Should have a vector of sensitivities for each curve name");
      }
      for (int i = 0; i < curves.size(); i++) {
        final String curve = (String) curves.get(i).getValue();
        final Currency currency = Currency.of((String) currencies.get(i).getValue());
        final FudgeMsg perCurveMessage = (FudgeMsg) sensitivitiesPerCurve.get(i).getValue();
        final List<FudgeField> perCurveFields = perCurveMessage.getAllByName(SENSITIVITY);
        final double[] values = new double[perCurveFields.size()];
        for (int j = 0; j < perCurveFields.size(); j++) {
          values[j] = (Double) perCurveFields.get(j).getValue();
        }
        sensitivities.put(Pairs.of(curve, currency), new DoubleMatrix1D(values));
      }
      return MultipleCurrencyParameterSensitivity.of(sensitivities);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final MultipleCurrencyParameterSensitivity object) {
      final Map<Pair<String, Currency>, DoubleMatrix1D> sensitivities = object.getSensitivities();
      for (final Map.Entry<Pair<String, Currency>, DoubleMatrix1D> entry : sensitivities.entrySet()) {
        final MutableFudgeMsg perCurveMessage = serializer.newMessage();
        message.add(CURVE_NAME, entry.getKey().getFirst());
        message.add(CURRENCY, entry.getKey().getSecond().getCode());
        final double[] sensitivity = entry.getValue().getData();
        for (final double d : sensitivity) {
          perCurveMessage.add(SENSITIVITY, d);
        }
        message.add(SENSITIVITIES_FOR_PAIR, perCurveMessage);
      }
    }

  }

  /**
   * Fudge builders for {@link CurveBuildingBlock}
   */
  @FudgeBuilderFor(CurveBuildingBlock.class)
  public static final class CurveBuildingBlockBuilder extends AbstractFudgeBuilder<CurveBuildingBlock> {
    /** The curve name field */
    private static final String CURVE_NAME = "curveName";
    /** The starting point of the curve parameters field */
    private static final String START_POINT = "startPoint";
    /** The number of parameters field */
    private static final String N_PARAMETERS = "nParameters";

    @Override
    public CurveBuildingBlock buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final LinkedHashMap<String, Pair<Integer, Integer>> data = new LinkedHashMap<>();
      final List<FudgeField> curveFields = message.getAllByName(CURVE_NAME);
      final List<FudgeField> startPointFields = message.getAllByName(START_POINT);
      final int size = curveFields.size();
      if (size != startPointFields.size()) {
        throw new OpenGammaRuntimeException("Should have one start point per curve name");
      }
      final List<FudgeField> nParametersFields = message.getAllByName(N_PARAMETERS);
      if (size != nParametersFields.size()) {
        throw new OpenGammaRuntimeException("Should have one number of parameters per curve name");
      }
      for (int i = 0; i < size; i++) {
        final String curveName = (String) curveFields.get(i).getValue();
        final Number startPointNumber = (Number) startPointFields.get(i).getValue();
        final Number nParametersNumber = (Number) nParametersFields.get(i).getValue();
        final Integer startPoint = startPointNumber.intValue();
        final Integer nParameters = nParametersNumber.intValue();
        data.put(curveName, Pairs.of(startPoint, nParameters));
      }
      return new CurveBuildingBlock(data);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final CurveBuildingBlock object) {
      for (final Map.Entry<String, Pair<Integer, Integer>> entry : object.getData().entrySet()) {
        message.add(CURVE_NAME, entry.getKey());
        message.add(START_POINT, entry.getValue().getFirst());
        message.add(N_PARAMETERS, entry.getValue().getSecond());
      }
    }

  }

  /**
   * Fudge builder for {@link CurveBuildingBlockBundle}
   */
  @FudgeBuilderFor(CurveBuildingBlockBundle.class)
  public static final class CurveBuildingBlockBundleBuilder extends AbstractFudgeBuilder<CurveBuildingBlockBundle> {
    /** The curve name field */
    private static final String CURVE_NAME = "curveName";
    /** The curve building block field */
    private static final String CURVE_BUILDING_BLOCK = "curveBuildingBlock";
    /** The Jacobian field */
    private static final String JACOBIAN = "jacobian";

    @Override
    public CurveBuildingBlockBundle buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> data = new LinkedHashMap<>();
      final List<FudgeField> curveNameFields = message.getAllByName(CURVE_NAME);
      final List<FudgeField> curveBuildingBlockFields = message.getAllByName(CURVE_BUILDING_BLOCK);
      final int size = curveNameFields.size();
      if (size != curveBuildingBlockFields.size()) {
        throw new OpenGammaRuntimeException("Should have one curve building block per curve name");
      }
      final List<FudgeField> jacobianFields = message.getAllByName(JACOBIAN);
      if (size != jacobianFields.size()) {
        throw new OpenGammaRuntimeException("Should have one Jacobian per curve name");
      }
      for (int i = 0; i < size; i++) {
        final String curveName = (String) curveNameFields.get(i).getValue();
        final CurveBuildingBlock curveBuildingBlock = deserializer.fieldValueToObject(CurveBuildingBlock.class, curveBuildingBlockFields.get(i));
        final DoubleMatrix2D jacobian = deserializer.fieldValueToObject(DoubleMatrix2D.class, jacobianFields.get(i));
        data.put(curveName, Pairs.of(curveBuildingBlock, jacobian));
      }
      return new CurveBuildingBlockBundle(data);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final CurveBuildingBlockBundle object) {
      for (final Map.Entry<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> entry : object.getData().entrySet()) {
        message.add(CURVE_NAME, entry.getKey());
        serializer.addToMessageWithClassHeaders(message, CURVE_BUILDING_BLOCK, null, entry.getValue().getFirst());
        serializer.addToMessageWithClassHeaders(message, JACOBIAN, null, entry.getValue().getSecond());
      }
    }

  }

  /**
   * Fudge builder for {@link InflationSensitivity}
   */
  @FudgeBuilderFor(InflationSensitivity.class)
  public static final class InflationSensitivityBuilder extends AbstractFudgeBuilder<InflationSensitivity> {
    /** The sensitivity field */
    private static final String IR_CURVE_SENSITIVITY = "irCurveSensitivity";
    /** The price curve name field */
    private static final String PRICE_CURVE_NAME = "priceCurveName";
    /** The price curve sensitivities per price curve name */
    private static final String SENSITIVITIES_PER_PRICE_CURVE = "sensitivitiesPerPriceCurve";
    /** The time field */
    private static final String TIME = "time";
    /** The sensitivity field*/
    private static final String SENSITIVITY = "sensitivity";

    @Override
    public InflationSensitivity buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final MulticurveSensitivity multicurveSensitivity = deserializer.fieldValueToObject(MulticurveSensitivity.class, message.getByName(IR_CURVE_SENSITIVITY));
      final Map<String, List<DoublesPair>> priceCurveSensitivity = new HashMap<>();
      final List<FudgeField> priceCurveNameFields = message.getAllByName(PRICE_CURVE_NAME);
      final List<FudgeField> sensitivitiesPerCurve = message.getAllByName(SENSITIVITIES_PER_PRICE_CURVE);
      if (priceCurveNameFields.size() != sensitivitiesPerCurve.size()) {
        throw new OpenGammaRuntimeException("Should have one set of price curve sensitivities per price curve name");
      }
      for (int i = 0; i < priceCurveNameFields.size(); i++) {
        final String priceCurveName = (String) priceCurveNameFields.get(i).getValue();
        final FudgeMsg priceCurveSensitivities = (FudgeMsg) sensitivitiesPerCurve.get(i).getValue();
        final List<DoublesPair> sensitivities = new ArrayList<>();
        final List<FudgeField> times = priceCurveSensitivities.getAllByName(TIME);
        final List<FudgeField> sensitivity = priceCurveSensitivities.getAllByName(SENSITIVITY);
        if (times.size() != sensitivity.size()) {
          throw new OpenGammaRuntimeException("Should have one sensitivity per time");
        }
        for (int j = 0; j < times.size(); j++) {
          Double time = (Double) times.get(j).getValue();
          Double sens = (Double) sensitivity.get(j).getValue();
          sensitivities.add(DoublesPair.of(time.doubleValue(), sens.doubleValue()));
        }
        priceCurveSensitivity.put(priceCurveName, sensitivities);
      }
      return InflationSensitivity.of(multicurveSensitivity, priceCurveSensitivity);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final InflationSensitivity object) {
      serializer.addToMessageWithClassHeaders(message, IR_CURVE_SENSITIVITY, null, object.getMulticurveSensitivities());
      for (final Map.Entry<String, List<DoublesPair>> entry : object.getPriceCurveSensitivities().entrySet()) {
        message.add(PRICE_CURVE_NAME, entry.getKey());
        final MutableFudgeMsg perCurveMessage = serializer.newMessage();
        for (final DoublesPair pair : entry.getValue()) {
          perCurveMessage.add(TIME, pair.getFirst());
          perCurveMessage.add(SENSITIVITY, pair.getSecond());
        }
        message.add(SENSITIVITIES_PER_PRICE_CURVE, perCurveMessage);
      }
    }

  }

  /**
   * Fudge builder for {@link MultipleCurrencyInflationSensitivity}
   */
  @FudgeBuilderFor(MultipleCurrencyInflationSensitivity.class)
  public static final class MultipleCurrencyInflationSensitivityBuilder extends AbstractFudgeBuilder<MultipleCurrencyInflationSensitivity> {
    /** The currencies field */
    private static final String CURRENCY = "currency";
    /** The sensitivities field */
    private static final String SENSITIVITIES = "sensitivities";

    @Override
    public MultipleCurrencyInflationSensitivity buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final List<FudgeField> currencies = message.getAllByName(CURRENCY);
      final List<FudgeField> sensitivities = message.getAllByName(SENSITIVITIES);
      if (currencies.size() != sensitivities.size()) {
        throw new OpenGammaRuntimeException("Should have same number of sensitivities as currencies");
      }
      MultipleCurrencyInflationSensitivity result = new MultipleCurrencyInflationSensitivity();
      for (int i = 0; i < currencies.size(); i++) {
        final Currency currency = Currency.of((String) currencies.get(i).getValue());
        final InflationSensitivity sensitivity = deserializer.fieldValueToObject(InflationSensitivity.class, sensitivities.get(i));
        result = result.plus(currency, sensitivity);
      }
      return result;
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final MultipleCurrencyInflationSensitivity object) {
      final Map<Currency, InflationSensitivity> sensitivities = object.getSensitivities();
      for (final Map.Entry<Currency, InflationSensitivity> entry : sensitivities.entrySet()) {
        message.add(CURRENCY, entry.getKey().getCode());
        serializer.addToMessageWithClassHeaders(message, SENSITIVITIES, null, entry.getValue());
      }
    }
  }
}
