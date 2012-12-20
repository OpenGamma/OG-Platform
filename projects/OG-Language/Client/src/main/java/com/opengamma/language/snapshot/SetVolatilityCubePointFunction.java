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
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilityCubeSnapshot;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.time.Tenor;

/**
 * Updates a point within a "volatility cube"
 */
public class SetVolatilityCubePointFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final SetVolatilityCubePointFunction INSTANCE = new SetVolatilityCubePointFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("snapshot", JavaTypeInfo.builder(ManageableVolatilityCubeSnapshot.class).get()),
        new MetaParameter("swapTenor", JavaTypeInfo.builder(Tenor.class).get()),
        new MetaParameter("optionExpiry", JavaTypeInfo.builder(Tenor.class).get()),
        new MetaParameter("relativeStrike", JavaTypeInfo.builder(Double.class).get()),
        new MetaParameter("overrideValue", JavaTypeInfo.builder(Double.class).allowNull().get()),
        new MetaParameter("marketValue", JavaTypeInfo.builder(Double.class).allowNull().get()));
  }

  // TODO: functions for the "other values", and "strikes" for a cube

  private SetVolatilityCubePointFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "SetVolatilityCubePoint", getParameters(), this));
  }

  protected SetVolatilityCubePointFunction() {
    this(new DefinitionAnnotater(SetVolatilityCubePointFunction.class));
  }

  public static ManageableVolatilityCubeSnapshot invoke(final ManageableVolatilityCubeSnapshot snapshot, final Tenor swapTenor, final Tenor optionExpiry, final double relativeStrike,
      final Double overrideValue, final Double marketValue) {
    final Map<VolatilityPoint, ValueSnapshot> points = snapshot.getValues();
    final VolatilityPoint key = new VolatilityPoint(swapTenor, optionExpiry, relativeStrike);
    if ((overrideValue != null) || (marketValue != null)) {
      final ValueSnapshot value = points.get(key);
      if (value != null) {
        if (marketValue != null) {
          points.put(key, new ValueSnapshot(marketValue, overrideValue));
        } else {
          value.setOverrideValue(overrideValue);
        }
      } else {
        points.put(key, new ValueSnapshot(marketValue, overrideValue));
      }
    } else {
      points.remove(key);
    }
    return snapshot;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ManageableVolatilityCubeSnapshot) parameters[0], (Tenor) parameters[1], (Tenor) parameters[2], (Double) parameters[3], (Double) parameters[4], (Double) parameters[5]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
