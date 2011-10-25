/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.opengamma.core.marketdatasnapshot.VolatilityCubeKey;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeSnapshot;
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
 * Updates a "volatility cube" component of a snapshot
 */
public class SetSnapshotVolatilityCubeFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final SetSnapshotVolatilityCubeFunction INSTANCE = new SetSnapshotVolatilityCubeFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("snapshot", JavaTypeInfo.builder(ManageableMarketDataSnapshot.class).get()),
        new MetaParameter("name", JavaTypeInfo.builder(String.class).get()),
        new MetaParameter("cube", JavaTypeInfo.builder(VolatilityCubeSnapshot.class).allowNull().get()));
  }

  private SetSnapshotVolatilityCubeFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "SetSnapshotVolatilityCube", getParameters(), this));
  }

  protected SetSnapshotVolatilityCubeFunction() {
    this(new DefinitionAnnotater(SetSnapshotVolatilityCubeFunction.class));
  }

  public static ManageableMarketDataSnapshot invoke(final ManageableMarketDataSnapshot snapshot, final String name, final VolatilityCubeSnapshot cube) {
    final int underscore = name.indexOf('_');
    if (underscore <= 0) {
      throw new InvokeInvalidArgumentException(1, "Invalid cube name");
    }
    final String cubeCurrency = name.substring(0, underscore);
    final String cubeName = name.substring(underscore + 1);
    final VolatilityCubeKey key = new VolatilityCubeKey(Currency.of(cubeCurrency), cubeName);
    if (snapshot.getVolatilityCubes() == null) {
      snapshot.setVolatilityCubes(new HashMap<VolatilityCubeKey, VolatilityCubeSnapshot>());
    }
    if (cube != null) {
      snapshot.getVolatilityCubes().put(key, cube);
    } else {
      snapshot.getVolatilityCubes().remove(key);
    }
    return snapshot;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ManageableMarketDataSnapshot) parameters[0], (String) parameters[1], (VolatilityCubeSnapshot) parameters[2]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
