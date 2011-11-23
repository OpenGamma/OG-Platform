/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.function.resolver.FunctionPriority;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.financial.analytics.FilteringSummingFunction;
import com.opengamma.financial.analytics.ircurve.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.model.bond.BondPV01CountryCurveFunction;
import com.opengamma.financial.analytics.model.bond.BondPV01CurrencyCurveFunction;
import com.opengamma.financial.analytics.model.bond.BondPresentValueCountryCurveFunction;
import com.opengamma.financial.analytics.model.bond.BondPresentValueCurrencyCurveFunction;
import com.opengamma.financial.currency.CurrencyConversionFunction;
import com.opengamma.financial.currency.CurrencyMatrixSourcingFunction;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Creates a FunctionResolver for the demo, prioritizing the functions
 */
public class DemoFunctionResolverFactoryBean extends SingletonFactoryBean<FunctionResolver> {

  private CompiledFunctionService _functionCompilationService;

  public void setFunctionCompilationService(final CompiledFunctionService functionCompilationService) {
    _functionCompilationService = functionCompilationService;
  }

  public CompiledFunctionService getFunctionCompilationService() {
    return _functionCompilationService;
  }

  public static FunctionResolver createFunctionResolver(final CompiledFunctionService functionCompilationSerice) {
    return new DefaultFunctionResolver(functionCompilationSerice, new FunctionPriority() {
      @Override
      public int getPriority(final CompiledFunctionDefinition function) {
        if (function instanceof CurrencyConversionFunction) {
          return Integer.MIN_VALUE;
        }
        if (function instanceof DefaultPropertyFunction) {
          final DefaultPropertyFunction defaultPropertyFunction = (DefaultPropertyFunction) function;
          if (defaultPropertyFunction.isPermitWithout()) {
            // Place below the filtering summing function priority, or the filter may never be applied.
            return -2;
          } else {
            // All other currency injections are important; e.g. the currency constraint can't be omitted for some functions
            return Integer.MAX_VALUE + defaultPropertyFunction.getPriority().getPriorityAdjust() - DefaultPropertyFunction.PriorityClass.MAX_ADJUST;
          }
        }
        if (function instanceof BondPresentValueCountryCurveFunction) {
          return 4;
        }
        if (function instanceof BondPresentValueCurrencyCurveFunction) {
          return 3;
        }
        if (function instanceof BondPV01CountryCurveFunction) {
          return 6;
        }
        if (function instanceof BondPV01CurrencyCurveFunction) {
          return 5;
        }
        if (function instanceof FilteringSummingFunction) {
          // Anything that filters should be lower priority than a conventional summing operation that can apply
          // to all of its inputs
          return -1;
        }
        if (function instanceof MarketInstrumentImpliedYieldCurveFunction) {
          MarketInstrumentImpliedYieldCurveFunction yieldCurveFunction = (MarketInstrumentImpliedYieldCurveFunction) function;
          return yieldCurveFunction.getPriority();
        }
        if (function instanceof CurrencyMatrixSourcingFunction) {
          CurrencyMatrixSourcingFunction currencyMatrixSourcingFunction = (CurrencyMatrixSourcingFunction) function;
          return currencyMatrixSourcingFunction.getPriority();
        }
        return 0;
      }
    });
  }

  @Override
  protected FunctionResolver createObject() {
    return createFunctionResolver(getFunctionCompilationService());
  }

}
