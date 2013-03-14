/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.Arrays;
import java.util.List;

import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.id.ExternalId;
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
 * Retrieves a "global value" component of a snapshot
 */
public class GetSnapshotGlobalValueFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final GetSnapshotGlobalValueFunction INSTANCE = new GetSnapshotGlobalValueFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("snapshot", JavaTypeInfo.builder(ManageableMarketDataSnapshot.class).get()),
        new MetaParameter("valueName", JavaTypeInfo.builder(String.class).get()),
        new MetaParameter("identifier", JavaTypeInfo.builder(ExternalId.class).get()));
  }

  private GetSnapshotGlobalValueFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "GetSnapshotGlobalValue", getParameters(), this));
  }

  protected GetSnapshotGlobalValueFunction() {
    this(new DefinitionAnnotater(GetSnapshotGlobalValueFunction.class));
  }

  public static List<Double> invoke(final ManageableMarketDataSnapshot snapshot, final String valueName, final ExternalId identifier) {
    final ValueSnapshot values = snapshot.getGlobalValues().getValue(identifier, valueName);
    if (values == null) {
      throw new InvokeInvalidArgumentException(0, "Snapshot does not contain value");
    }
    return Arrays.asList(values.getOverrideValue(), values.getMarketValue());
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ManageableMarketDataSnapshot) parameters[0], (String) parameters[1], (ExternalId) parameters[2]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
