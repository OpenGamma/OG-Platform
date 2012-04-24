/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.position;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.id.ExternalId;
import com.opengamma.language.async.AsynchronousExecution;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Creates a copy of a trade with its {@link SimpleTrade#setProviderId(ExternalId) provider ID} set.
 */
public class SetTradeProviderIdFunction extends AbstractFunctionInvoker implements PublishedFunction {

  private static final List<MetaParameter> s_parameters = ImmutableList.of(
      new MetaParameter("trade", JavaTypeInfo.builder(Trade.class).get()),
      new MetaParameter("providerId", JavaTypeInfo.builder(ExternalId.class).get()));

  private static final DefinitionAnnotater s_annotater = new DefinitionAnnotater(SetTradeProviderIdFunction.class);

  public static final SetTradeProviderIdFunction INSTANCE = new SetTradeProviderIdFunction(s_annotater);

  private final MetaFunction _meta;

  private SetTradeProviderIdFunction(DefinitionAnnotater info) {
    super(info.annotate(s_parameters));
    _meta = new MetaFunction(Categories.POSITION, "SetTradeProviderId", s_parameters, this);
  }

  @Override
  protected Object invokeImpl(SessionContext sessionContext, Object[] parameters) throws AsynchronousExecution {
    Trade trade = (Trade) parameters[0];
    ExternalId providerId = (ExternalId) parameters[1];
    SimpleTrade newTrade = new SimpleTrade(trade);
    newTrade.setProviderId(providerId);
    return newTrade;
  }

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }
}
