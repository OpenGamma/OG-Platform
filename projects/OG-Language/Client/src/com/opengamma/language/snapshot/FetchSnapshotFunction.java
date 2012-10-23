/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.Arrays;
import java.util.List;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.id.UniqueId;
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
 * Fetches a snapshot from the {@link MarketDataSnapshotSource}.
 */
public class FetchSnapshotFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final FetchSnapshotFunction INSTANCE = new FetchSnapshotFunction();

  private final MetaFunction _meta;

  private static final int IDENTIFIER = 0;

  private static List<MetaParameter> parameters() {
    final MetaParameter identifierParameter = new MetaParameter("identifier", JavaTypeInfo.builder(UniqueId.class).get());
    return Arrays.asList(identifierParameter);
  }

  private FetchSnapshotFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "FetchSnapshot", getParameters(), this));
  }

  protected FetchSnapshotFunction() {
    this(new DefinitionAnnotater(FetchSnapshotFunction.class));
  }

  public static StructuredMarketDataSnapshot invoke(final SessionContext context, final UniqueId uid) {
    try {
      return context.getGlobalContext().getMarketDataSnapshotSource().get(uid);
    } catch (DataNotFoundException ex) {
      throw new InvokeInvalidArgumentException(IDENTIFIER, "Identifier not found");
    } catch (IllegalArgumentException e) {
      throw new InvokeInvalidArgumentException(IDENTIFIER, "Identifier is not valid");
    }
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke(sessionContext, (UniqueId) parameters[IDENTIFIER]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
