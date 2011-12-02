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

/**
 * Updates a "volatility cube" component of a snapshot
 */
public class SetSnapshotVolatilityCubeFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final SetSnapshotVolatilityCubeFunction INSTANCE = new SetSnapshotVolatilityCubeFunction();

  private final MetaFunction _meta;

  private static final int SNAPSHOT = 0;
  private static final int NAME = 1;
  private static final int CUBE = 2;

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
    if (snapshot.getVolatilityCubes() == null) {
      snapshot.setVolatilityCubes(new HashMap<VolatilityCubeKey, VolatilityCubeSnapshot>());
    }
    final VolatilityCubeKey key = StructuredMarketDataSnapshotUtil.toVolatilityCubeKey(name);
    if (key == null) {
      throw new InvokeInvalidArgumentException(NAME, "Invalid cube name");
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
    return invoke((ManageableMarketDataSnapshot) parameters[SNAPSHOT], (String) parameters[NAME], (VolatilityCubeSnapshot) parameters[CUBE]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
