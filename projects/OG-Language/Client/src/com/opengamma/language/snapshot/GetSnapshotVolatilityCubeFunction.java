/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.Arrays;
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
 * Fetches a "volatility cube" component of a snapshot
 */
public class GetSnapshotVolatilityCubeFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final GetSnapshotVolatilityCubeFunction INSTANCE = new GetSnapshotVolatilityCubeFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("snapshot", JavaTypeInfo.builder(ManageableMarketDataSnapshot.class).get()),
        new MetaParameter("name", JavaTypeInfo.builder(String.class).get()));
  }

  private GetSnapshotVolatilityCubeFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "GetSnapshotVolatilityCube", getParameters(), this));
  }

  protected GetSnapshotVolatilityCubeFunction() {
    this(new DefinitionAnnotater(GetSnapshotVolatilityCubeFunction.class));
  }

  public static VolatilityCubeSnapshot invoke(final ManageableMarketDataSnapshot snapshot, final String name) {
    final int underscore = name.indexOf('_');
    if (underscore <= 0) {
      throw new InvokeInvalidArgumentException(1, "Invalid cube name");
    }
    final String cubeCurrency = name.substring(0, underscore);
    final String cubeName = name.substring(underscore + 1);
    final VolatilityCubeKey key = new VolatilityCubeKey(Currency.of(cubeCurrency), cubeName);
    if (snapshot.getVolatilityCubes() == null) {
      throw new InvokeInvalidArgumentException(0, "No cubes in snapshot");
    }
    final VolatilityCubeSnapshot cubeSnapshot = snapshot.getVolatilityCubes().get(key);
    if (cubeSnapshot == null) {
      throw new InvokeInvalidArgumentException(1, "Cube not found in snapshot");
    }
    return cubeSnapshot;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ManageableMarketDataSnapshot) parameters[0], (String) parameters[1]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
