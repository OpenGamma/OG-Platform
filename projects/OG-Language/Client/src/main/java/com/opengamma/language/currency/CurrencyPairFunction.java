/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.currency;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairsSource;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 * Returns the market convention {@link com.opengamma.financial.currency.CurrencyPair} for the two currencies in an FX trade.
 */
public class CurrencyPairFunction implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final CurrencyPairFunction INSTANCE = new CurrencyPairFunction();

  private final MetaFunction _metaFunction;

  public CurrencyPairFunction() {
    MetaParameter currency1 = new MetaParameter("currency1", JavaTypeInfo.builder(Currency.class).get());
    MetaParameter currency2 = new MetaParameter("currency2", JavaTypeInfo.builder(Currency.class).get());
    MetaParameter currencyPairsName = new MetaParameter("currencyPairsName", JavaTypeInfo.builder(String.class).allowNull().get());
    currencyPairsName.setDescription("Name of the set of market convention currency pairs");
    List<MetaParameter> params = ImmutableList.of(currency1, currency2, currencyPairsName);
    _metaFunction = new MetaFunction(Categories.CURRENCY, "CurrencyPair", params, new Invoker(params));
    _metaFunction.setDescription("Returns the market convention currency pair for the two currencies");
  }

  /**
   * Returns the market convention {@link com.opengamma.financial.currency.CurrencyPair} for the two currencies in an FX trade.
   * @param context The context
   * @param currency1 One currency from the trade
   * @param currency2 The trade's other currency
   * @param currencyPairsName The name of the set of market convention currency pairs.
   *                          If this is omitted the default set is used.
   * @return The market convention currency pair for the two currencies
   */
  public CurrencyPair execute(SessionContext context, Currency currency1, Currency currency2, String currencyPairsName) {
    ArgumentChecker.notNull(currency1, "currency1");
    ArgumentChecker.notNull(currency2, "currency2");
    CurrencyPairsSource currencyPairsSource = context.getGlobalContext().getCurrencyPairsSource();
    return currencyPairsSource.getCurrencyPair(currencyPairsName, currency1, currency2);
  }

  @Override
  public MetaFunction getMetaFunction() {
    return _metaFunction;
  }
 
  private class Invoker extends AbstractFunctionInvoker {

    protected Invoker(final List<MetaParameter> parameters) {
      super(parameters);
    }

    @Override
    protected Object invokeImpl(SessionContext sessionContext, Object[] parameters) throws AsynchronousExecution {
      Currency currency1 = (Currency) parameters[0];
      Currency currency2 = (Currency) parameters[1];
      String currencyPairsName;
      if (parameters.length > 2) {
        currencyPairsName = (String) parameters[2];
      } else {
        currencyPairsName = null;
      }
      return execute(sessionContext, currency1, currency2, currencyPairsName);
    }
  }
}
