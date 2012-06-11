/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.currency;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.financial.currency.CurrencyPairsSource;
import com.opengamma.financial.currency.CurrencyUtils;
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
 * Returns an FX rate for a currency trade quoted using the market convention currency pair.
 */
public class FxRateFunction implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final FxRateFunction INSTANCE = new FxRateFunction();

  private final MetaFunction _metaFunction;

  public FxRateFunction() {
    MetaParameter currency1 = new MetaParameter("currency1", JavaTypeInfo.builder(Currency.class).get());
    MetaParameter currency2 = new MetaParameter("currency2", JavaTypeInfo.builder(Currency.class).get());
    MetaParameter amount1 = new MetaParameter("amount1", JavaTypeInfo.builder(Double.class).get());
    amount1.setDescription("The amount in currency1");
    MetaParameter amount2 = new MetaParameter("amount2", JavaTypeInfo.builder(Double.class).get());
    amount2.setDescription("The amount in currency2");
    MetaParameter currencyPairsName = new MetaParameter("currencyPairsName", JavaTypeInfo.builder(String.class).allowNull().get());
    currencyPairsName.setDescription("Name of the set of market convention currency pairs");
    List<MetaParameter> params = ImmutableList.of(currency1, currency2, amount1, amount2, currencyPairsName);
    _metaFunction = new MetaFunction(Categories.CURRENCY, "FXRate", params, new Invoker(params));
    _metaFunction.setDescription("Returns the FX rate quoted using the market convention currency pair");
  }

  /**
   * Returns an FX rate quoted using the market convention currency pair.  If there is no market convention
   * currency pair found for the currencies then this function returns null.  It will also return null
   * if no market convention currency pairs can be found with the specified name.
   * @param context The context
   * @param currency1 One currency from the trade
   * @param amount1 The amount in {@code currency1}
   * @param currency2 The trade's other currency
   * @param amount2 The amount in {@code currency2}
   * @param currencyPairsName The name of the set of market convention currency pairs.
   *                          If this is omitted the default set is used.
   * @return The FX rate for the trade quoted using the market convention currency pair
   */
  public Double execute(SessionContext context,
                        Currency currency1,
                        Currency currency2,
                        double amount1,
                        double amount2,
                        String currencyPairsName) {
    ArgumentChecker.notNull(currency1, "currency1");
    ArgumentChecker.notNull(currency2, "currency2");
    CurrencyPairsSource currencyPairsSource = context.getGlobalContext().getCurrencyPairsSource();
    return CurrencyUtils.getRate(currency1, currency2, amount1, amount2, currencyPairsSource, currencyPairsName);
  }

  @Override
  public MetaFunction getMetaFunction() {
    return _metaFunction;
  }

  private class Invoker extends AbstractFunctionInvoker {
    
    public Invoker(List<MetaParameter> params) {
      super(params);
    }

    @Override
    protected Object invokeImpl(SessionContext sessionContext, Object[] parameters) throws AsynchronousExecution {
      Currency currency1 = (Currency) parameters[0];
      Currency currency2 = (Currency) parameters[1];
      Double amount1 = (Double) parameters[2];
      Double amount2 = (Double) parameters[3];
      String currencyPairsName;
      if (parameters.length > 4) {
        currencyPairsName = (String) parameters[4];
      } else {
        currencyPairsName = null;
      }
      return execute(sessionContext, currency1, currency2, amount1, amount2, currencyPairsName);
    }
  }
}
