/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import java.util.Arrays;
import java.util.List;

import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Creates a {@link ViewCalculationRate} configuration item.
 */
public class ViewCalculationRateFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final ViewCalculationRateFunction INSTANCE = new ViewCalculationRateFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("maxDelta", JavaTypeInfo.builder(Long.class).allowNull().get()),
        new MetaParameter("minDelta", JavaTypeInfo.builder(Long.class).allowNull().get()),
        new MetaParameter("maxFull", JavaTypeInfo.builder(Long.class).allowNull().get()),
        new MetaParameter("minFull", JavaTypeInfo.builder(Long.class).allowNull().get()));
  }

  private ViewCalculationRateFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.VIEW, "ViewCalculationRate", getParameters(), this));
  }

  protected ViewCalculationRateFunction() {
    this(new DefinitionAnnotater(ViewCalculationRateFunction.class));
  }

  private static long getLong(final Long l1, final Long l2, final Long l3, final Long l4) {
    if (l1 != null) {
      return l1;
    } else if (l2 != null) {
      return l2;
    } else if (l3 != null) {
      return l3;
    } else if (l4 != null) {
      return l4;
    } else {
      throw new IllegalArgumentException("At least one parameter must be supplied");
    }
  }

  private static InvokeInvalidArgumentException createIAE(final int index, final long minDelta, final long maxDelta, final long minFull, final long maxFull) {
    return new InvokeInvalidArgumentException(index, "Invalid recalculation time - minDelta = " + minDelta + "ms, maxDelta = " + maxDelta + "ms, minFull = " + minFull + "ms, maxFull = " + maxFull +
        "ms");
  }

  public static ViewCalculationRate invoke(final Long maxDelta, final Long minDelta, final Long maxFull, final Long minFull) {
    final ViewCalculationRate vcr = new ViewCalculationRate(
        getLong(minDelta, maxDelta, minFull, maxFull),
        getLong(maxDelta, minDelta, maxFull, minFull),
        getLong(minFull, maxFull, minDelta, maxDelta),
        getLong(maxFull, minFull, maxDelta, minDelta));
    if (vcr.getMinDelta() <= 0) {
      throw createIAE(1, minDelta, maxDelta, minFull, maxFull);
    }
    if (vcr.getMaxDelta() < vcr.getMaxDelta()) {
      throw createIAE(0, minDelta, maxDelta, minFull, maxFull);
    }
    if (vcr.getMinFull() <= 0) {
      throw createIAE(3, minDelta, maxDelta, minFull, maxFull);
    }
    if (vcr.getMaxFull() < vcr.getMinFull()) {
      throw createIAE(2, minDelta, maxDelta, minFull, maxFull);
    }
    return vcr;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((Long) parameters[0], (Long) parameters[1], (Long) parameters[2], (Long) parameters[3]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
