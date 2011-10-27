/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.money.Currency;

/**
 * Updates a "yield curve" component of a snapshot
 */
public class SetSnapshotYieldCurveFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final SetSnapshotYieldCurveFunction INSTANCE = new SetSnapshotYieldCurveFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("snapshot", JavaTypeInfo.builder(ManageableMarketDataSnapshot.class).get()),
        new MetaParameter("name", JavaTypeInfo.builder(String.class).get()),
        new MetaParameter("yieldCurve", JavaTypeInfo.builder(YieldCurveSnapshot.class).allowNull().get()));
  }

  private SetSnapshotYieldCurveFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "SetSnapshotYieldCurve", getParameters(), this));
  }

  protected SetSnapshotYieldCurveFunction() {
    this(new DefinitionAnnotater(SetSnapshotYieldCurveFunction.class));
  }

  public static ManageableMarketDataSnapshot invoke(final ManageableMarketDataSnapshot snapshot, final String name, final YieldCurveSnapshot yieldCurve) {
    final int underscore = name.indexOf('_');
    if (underscore <= 0) {
      throw new InvokeInvalidArgumentException(1, "Invalid curve name");
    }
    final String curveCurrency = name.substring(0, underscore);
    final String curveName = name.substring(underscore + 1);
    final YieldCurveKey key = new YieldCurveKey(Currency.of(curveCurrency), curveName);
    if (snapshot.getYieldCurves() == null) {
      snapshot.setYieldCurves(new HashMap<YieldCurveKey, YieldCurveSnapshot>());
    }
    if (yieldCurve != null) {
      snapshot.getYieldCurves().put(key, yieldCurve);
    } else {
      snapshot.getYieldCurves().remove(key);
    }
    return snapshot;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ManageableMarketDataSnapshot) parameters[0], (String) parameters[1], (YieldCurveSnapshot) parameters[2]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
