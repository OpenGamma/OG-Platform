/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilitySurfaceSnapshot;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.tuple.Pair;

/**
 * Updates a point on a "volatility surface"
 */
public class SetVolatilitySurfacePointFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final SetVolatilitySurfacePointFunction INSTANCE = new SetVolatilitySurfacePointFunction();

  private final MetaFunction _meta;

  private static final int SNAPSHOT = 0;
  private static final int X = 1;
  private static final int Y = 2;
  private static final int OVERRIDE_VALUE = 3;
  private static final int MARKET_VALUE = 4;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("snapshot", JavaTypeInfo.builder(ManageableVolatilitySurfaceSnapshot.class).get()),
        new MetaParameter("x", JavaTypeInfo.builder(String.class).get()),
        new MetaParameter("y", JavaTypeInfo.builder(String.class).get()),
        new MetaParameter("overrideValue", JavaTypeInfo.builder(Double.class).allowNull().get()),
        new MetaParameter("marketValue", JavaTypeInfo.builder(Double.class).allowNull().get()));
  }

  private SetVolatilitySurfacePointFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "SetVolatilitySurfacePoint", getParameters(), this));
  }

  protected SetVolatilitySurfacePointFunction() {
    this(new DefinitionAnnotater(SetVolatilitySurfacePointFunction.class));
  }

  // TODO: This is awkward because the message representation is based on "objects" which is too vague. We can't therefore
  // put new points in, but only update points by matching on the strings the X & Y points produce.

  public static ManageableVolatilitySurfaceSnapshot invoke(final ManageableVolatilitySurfaceSnapshot snapshot, final String x, final String y, final Double overrideValue, final Double marketValue) {
    for (Map.Entry<Pair<Object, Object>, ValueSnapshot> surfacePoint : snapshot.getValues().entrySet()) {
      final Object xObject = surfacePoint.getKey().getFirst();
      if (x.equals(StructuredMarketDataSnapshotUtil.toString(xObject))) {
        final Object yObject = surfacePoint.getKey().getSecond();
        if (y.equals(StructuredMarketDataSnapshotUtil.toString(yObject))) {
          if (marketValue != null) {
            snapshot.getValues().put(surfacePoint.getKey(), new ValueSnapshot(marketValue, overrideValue));
          } else {
            surfacePoint.getValue().setOverrideValue(overrideValue);
          }
          break;
        }
      }
    }
    return snapshot;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ManageableVolatilitySurfaceSnapshot) parameters[SNAPSHOT], (String) parameters[X], (String) parameters[Y], (Double) parameters[OVERRIDE_VALUE], (Double) parameters[MARKET_VALUE]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
