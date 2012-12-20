/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorMixedLogNormal;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSABR;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSpline;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.WeightingFunction;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.WeightingFunctionFactory;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionFactory;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceInterpolator;
import com.opengamma.analytics.math.interpolation.Interpolator1D;

/**
 *
 */
/* package */ final class ModelVolatilitySmile {

  private ModelVolatilitySmile() {
  }

  @FudgeBuilderFor(VolatilitySurfaceInterpolator.class)
  public static final class VolatilitySurfaceInterpolatorBuilder extends AbstractFudgeBuilder<VolatilitySurfaceInterpolator> {
    private static final String SMILE_INTERPOLATOR_FIELD_NAME = "smileInterpolatorField";
    private static final String TIME_INTERPOLATOR_FIELD_NAME = "timeInterpolatorField";
    private static final String LOG_TIME_FIELD_NAME = "logTimeField";
    private static final String INTEGRATED_VARIANCE_FIELD_NAME = "integratedVarianceField";
    private static final String LOG_VALUE_FIELD_NAME = "logValueField";

    @Override
    public VolatilitySurfaceInterpolator buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final GeneralSmileInterpolator smileInterpolator = deserializer.fieldValueToObject(GeneralSmileInterpolator.class, message.getByName(SMILE_INTERPOLATOR_FIELD_NAME));
      final Interpolator1D timeInterpolator = deserializer.fieldValueToObject(Interpolator1D.class, message.getByName(TIME_INTERPOLATOR_FIELD_NAME));
      final boolean useLogTime = message.getBoolean(LOG_TIME_FIELD_NAME);
      final boolean useIntegratedVariance = message.getBoolean(INTEGRATED_VARIANCE_FIELD_NAME);
      final boolean useLogValue = message.getBoolean(LOG_VALUE_FIELD_NAME);
      return new VolatilitySurfaceInterpolator(smileInterpolator, timeInterpolator, useLogTime, useIntegratedVariance, useLogValue);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final VolatilitySurfaceInterpolator object) {
      serializer.addToMessage(message, SMILE_INTERPOLATOR_FIELD_NAME, null, object.getSmileInterpolator());
      serializer.addToMessage(message, TIME_INTERPOLATOR_FIELD_NAME, null, object.getTimeInterpolator());
      message.add(LOG_TIME_FIELD_NAME, null, object.useLogTime());
      message.add(INTEGRATED_VARIANCE_FIELD_NAME, null, object.useIntegratedVariance());
      message.add(LOG_VALUE_FIELD_NAME, null, object.useLogValue());
    }
  }

  @FudgeBuilderFor(SmileInterpolatorSpline.class)
  public static final class SmileInterpolatorSplineBuilder extends AbstractFudgeBuilder<SmileInterpolatorSpline> {
    private static final String INTERPOLATOR_FIELD_NAME = "interpolatorField";
    private static final String EXTRAPOLATOR_FAILURE_BEHAVIOUR_FIELD_NAME = "extrapolatorFailureBehaviourField";

    @Override
    public SmileInterpolatorSpline buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Interpolator1D interpolator = deserializer.fieldValueToObject(Interpolator1D.class, message.getByName(INTERPOLATOR_FIELD_NAME));
      final String extrapolatorFailureBehaviourName = message.getString(EXTRAPOLATOR_FAILURE_BEHAVIOUR_FIELD_NAME);
      return new SmileInterpolatorSpline(interpolator, extrapolatorFailureBehaviourName);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final SmileInterpolatorSpline object) {
      serializer.addToMessage(message, INTERPOLATOR_FIELD_NAME, null, object.getInterpolator());
      message.add(EXTRAPOLATOR_FAILURE_BEHAVIOUR_FIELD_NAME, object.getExtrapolatorFailureBehaviour());
    }
  }

  @FudgeBuilderFor(SmileInterpolatorMixedLogNormal.class)
  public static final class SmileInterpolatorMixedLogNormalBuilder extends AbstractFudgeBuilder<SmileInterpolatorMixedLogNormal> {
    private static final String WEIGHTING_FUNCTION_FIELD_NAME = "weightingFunctionField";

    @Override
    public SmileInterpolatorMixedLogNormal buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String weightingFunctionName = message.getString(WEIGHTING_FUNCTION_FIELD_NAME);
      return new SmileInterpolatorMixedLogNormal(WeightingFunctionFactory.getWeightingFunction(weightingFunctionName));
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final SmileInterpolatorMixedLogNormal object) {
      message.add(WEIGHTING_FUNCTION_FIELD_NAME, WeightingFunctionFactory.getWeightingFunctionName(object.getWeightingFunction()));
    }
  }

  @FudgeBuilderFor(SmileInterpolatorSABR.class)
  public static final class SmileInterpolatorSABRBuilder extends AbstractFudgeBuilder<SmileInterpolatorSABR> {
    private static final String VOLATILITY_MODEL_FIELD_NAME = "volatilityModelField";
    private static final String EXTERNAL_BETA_FIELD_NAME = "externalBetaField";
    private static final String BETA_FIELD_NAME = "betaField";
    private static final String WEIGHTING_FUNCTION_FIELD_NAME = "weightingFunctionField";

    @Override
    public SmileInterpolatorSABR buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String volatilityFunctionName = message.getString(VOLATILITY_MODEL_FIELD_NAME);
      final String weightingFunctionName = message.getString(WEIGHTING_FUNCTION_FIELD_NAME);
      final boolean externalBeta = message.getBoolean(EXTERNAL_BETA_FIELD_NAME);
      @SuppressWarnings("unchecked")
      final VolatilityFunctionProvider<SABRFormulaData> model = (VolatilityFunctionProvider<SABRFormulaData>) VolatilityFunctionFactory.getCalculator(volatilityFunctionName);
      final WeightingFunction weightingFunction = WeightingFunctionFactory.getWeightingFunction(weightingFunctionName);
      if (externalBeta) {
        final double beta = message.getDouble(BETA_FIELD_NAME);
        return new SmileInterpolatorSABR(model, beta, weightingFunction);
      }
      return new SmileInterpolatorSABR(model, weightingFunction);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final SmileInterpolatorSABR object) {
      message.add(VOLATILITY_MODEL_FIELD_NAME, VolatilityFunctionFactory.getCalculatorName(object.getModel()));
      message.add(WEIGHTING_FUNCTION_FIELD_NAME, WeightingFunctionFactory.getWeightingFunctionName(object.getWeightingFunction()));
      message.add(EXTERNAL_BETA_FIELD_NAME, object.useExternalBeta());
      if (object.useExternalBeta()) {
        message.add(BETA_FIELD_NAME, object.getBeta());
      }
    }

  }
}
