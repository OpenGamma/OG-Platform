/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableCurveSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableYieldCurveSnapshot;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Fetches the data from a curve as a 1D matrix tensor.
 */
public class GetCurveTensorFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final GetCurveTensorFunction INSTANCE = new GetCurveTensorFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("snapshot", JavaTypeInfo.builder(ManageableCurveSnapshot.class).get()),
        new MetaParameter("marketValue", JavaTypeInfo.builder(Boolean.class).defaultValue(Boolean.TRUE).get()),
        new MetaParameter("overrideValue", JavaTypeInfo.builder(Boolean.class).defaultValue(Boolean.FALSE).get()));
  }

  private GetCurveTensorFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "GetCurveTensor", getParameters(), this));
  }

  protected GetCurveTensorFunction() {
    this(new DefinitionAnnotater(GetCurveTensorFunction.class));
  }

  public static Value[] invoke(final ManageableCurveSnapshot snapshot, final Boolean marketValue, final Boolean overrideValue) {
    final List<Value> values = new LinkedList<Value>();
    for (final ExternalIdBundle target : snapshot.getValues().getTargets()) {
      final Map<String, ValueSnapshot> entries = snapshot.getValues().getTargetValues(target);
      for (final ValueSnapshot entry : entries.values()) {
        if (Boolean.TRUE.equals(overrideValue) && (entry.getOverrideValue() != null)) {
          values.add(ValueUtils.of(entry.getOverrideValue()));
          continue;
        }
        if (Boolean.TRUE.equals(marketValue) && (entry.getMarketValue() != null)) {
          values.add(ValueUtils.of(entry.getMarketValue()));
          continue;
        }
        values.add(new Value());
      }
    }
    return values.toArray(new Value[values.size()]);
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ManageableCurveSnapshot) parameters[0], (Boolean) parameters[1], (Boolean) parameters[2]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
