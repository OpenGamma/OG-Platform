/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableYieldCurveSnapshot;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.language.Value;
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
 * Modifies a yield curve to take values from the updated 1D matrix tensor.
 */
public class SetYieldCurveTensorFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final SetYieldCurveTensorFunction INSTANCE = new SetYieldCurveTensorFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("snapshot", JavaTypeInfo.builder(ManageableYieldCurveSnapshot.class).get()),
        new MetaParameter("overrideValue", JavaTypeInfo.builder(Value.class).arrayOf().allowNull().get()),
        new MetaParameter("marketValue", JavaTypeInfo.builder(Value.class).arrayOf().allowNull().get()));
  }

  private SetYieldCurveTensorFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "SetYieldCurveTensor", getParameters(), this));
  }

  protected SetYieldCurveTensorFunction() {
    this(new DefinitionAnnotater(SetYieldCurveTensorFunction.class));
  }

  public static ManageableYieldCurveSnapshot invoke(final ManageableYieldCurveSnapshot snapshot, final Value[] overrideValue, final Value[] marketValue) {
    int i = 0;
    final ManageableUnstructuredMarketDataSnapshot values = snapshot.getValues();
    for (final ExternalIdBundle target : values.getTargets()) {
      final Map<String, ValueSnapshot> entries = values.getTargetValues(target);
      if (marketValue != null) {
        for (final Map.Entry<String, ValueSnapshot> entry : new ArrayList<Map.Entry<String, ValueSnapshot>>(entries.entrySet())) {
          final Double override;
          if (overrideValue != null) {
            if (overrideValue.length < i) {
              throw new InvokeInvalidArgumentException(1, "Vector too short");
            }
            override = overrideValue[i].getDoubleValue();
          } else {
            override = entry.getValue().getOverrideValue();
          }
          values.putValue(target, entry.getKey(), new ValueSnapshot(marketValue[i].getDoubleValue(), override));
          i++;
        }
      } else {
        if (overrideValue != null) {
          for (final ValueSnapshot entry : entries.values()) {
            if (overrideValue.length < i) {
              throw new InvokeInvalidArgumentException(1, "Vector too short");
            }
            entry.setOverrideValue(overrideValue[i].getDoubleValue());
            i++;
          }
        }
      }
    }
    return snapshot;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ManageableYieldCurveSnapshot) parameters[0], (Value[]) parameters[1], (Value[]) parameters[2]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
