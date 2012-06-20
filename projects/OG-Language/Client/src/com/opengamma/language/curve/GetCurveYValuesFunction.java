/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.curve;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Returns a set of Y values for the given X values of a curve.
 */
public class GetCurveYValuesFunction extends AbstractFunctionInvoker implements PublishedFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(GetCurveYValuesFunction.class);

  /**
   * Default instance.
   */
  public static final GetCurveYValuesFunction INSTANCE = new GetCurveYValuesFunction();

  private final MetaFunction _meta;

  private static final int CURVE = 0;
  private static final int X_VALUES = 1;

  private static List<MetaParameter> parameters() {
    final MetaParameter curve = new MetaParameter("curve", JavaTypeInfo.builder(Curve.class).get());
    final MetaParameter xValues = new MetaParameter("xValues", JavaTypeInfo.builder(Double[].class).get());
    return Arrays.asList(curve, xValues);
  }

  private GetCurveYValuesFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.CURVE, "GetCurveYValues", getParameters(), this));
  }

  protected GetCurveYValuesFunction() {
    this(new DefinitionAnnotater(GetCurveYValuesFunction.class));
  }

  public static Double[] invoke(final Curve<Double, Double> curve, final Double[] xValues) {
    try {
      final Double[] result = new Double[xValues.length];
      for (int i = 0; i < xValues.length; i++) {
        result[i] = curve.getYValue(xValues[i]);
      }
      return result;
    } catch (ClassCastException e) {
      s_logger.warn("Caught exception {} from curve {}", e, curve);
      s_logger.debug("Caught exception", e);
      throw new InvokeInvalidArgumentException(CURVE, "Not a Double/Double curve");
    } catch (IllegalArgumentException e) {
      s_logger.warn("Caught exception {} from curve {}", e, curve);
      s_logger.debug("Caught exception", e);
      throw new InvokeInvalidArgumentException(X_VALUES, e.getMessage());
    }
  }

  // AbstractFunctionInvoker

  @SuppressWarnings("unchecked")
  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) throws AsynchronousExecution {
    return invoke((Curve<Double, Double>) parameters[CURVE], (Double[]) parameters[X_VALUES]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
