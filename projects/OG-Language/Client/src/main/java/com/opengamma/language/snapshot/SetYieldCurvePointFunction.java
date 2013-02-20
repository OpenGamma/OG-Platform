/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.Arrays;
import java.util.List;

import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableYieldCurveSnapshot;
import com.opengamma.id.ExternalId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Updates a point on a "yield curve"
 */
public class SetYieldCurvePointFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final SetYieldCurvePointFunction INSTANCE = new SetYieldCurvePointFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("snapshot", JavaTypeInfo.builder(ManageableYieldCurveSnapshot.class).get()),
        new MetaParameter("valueName", JavaTypeInfo.builder(String.class).get()),
        new MetaParameter("identifier", JavaTypeInfo.builder(ExternalId.class).get()),
        new MetaParameter("overrideValue", JavaTypeInfo.builder(Double.class).allowNull().get()),
        new MetaParameter("marketValue", JavaTypeInfo.builder(Double.class).allowNull().get()));
  }

  // TODO: update the valuation time for the curve

  private SetYieldCurvePointFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "SetYieldCurvePoint", getParameters(), this));
  }

  protected SetYieldCurvePointFunction() {
    this(new DefinitionAnnotater(SetYieldCurvePointFunction.class));
  }

  public static ManageableYieldCurveSnapshot invoke(final ManageableYieldCurveSnapshot snapshot, final String valueName, final ExternalId identifier,
      final Double overrideValue, final Double marketValue) {
    if (snapshot.getValues() == null) {
      snapshot.setValues(new ManageableUnstructuredMarketDataSnapshot());
    }
    UnstructuredMarketDataSnapshotUtil.setValue(snapshot.getValues(), valueName, identifier, overrideValue, marketValue);
    return snapshot;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ManageableYieldCurveSnapshot) parameters[0], (String) parameters[1], (ExternalId) parameters[2], (Double) parameters[3], (Double) parameters[4]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
