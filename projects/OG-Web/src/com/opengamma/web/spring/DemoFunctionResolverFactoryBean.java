/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver.FunctionPriority;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.financial.analytics.model.bond.BondPV01CountryCurveFunction;
import com.opengamma.financial.analytics.model.bond.BondPV01CurrencyCurveFunction;
import com.opengamma.financial.analytics.model.bond.BondPresentValueCountryCurveFunction;
import com.opengamma.financial.analytics.model.bond.BondPresentValueCurrencyCurveFunction;
import com.opengamma.financial.analytics.model.bond.BondZSpreadCountryCurveFunction;
import com.opengamma.financial.analytics.model.bond.BondZSpreadCurrencyCurveFunction;
import com.opengamma.financial.currency.CurrencyConversionFunction;
import com.opengamma.financial.currency.DefaultCurrencyFunction;
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
        if (function instanceof DefaultCurrencyFunction) {
          return Integer.MAX_VALUE;
        }
        if (function instanceof BondZSpreadCountryCurveFunction) {
          return 2;
        }
        if (function instanceof BondZSpreadCurrencyCurveFunction) {
          return 1;
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
        return 0;
      }
    });
  }

  @Override
  protected FunctionResolver createObject() {
    return createFunctionResolver(getFunctionCompilationService());
  }

}
