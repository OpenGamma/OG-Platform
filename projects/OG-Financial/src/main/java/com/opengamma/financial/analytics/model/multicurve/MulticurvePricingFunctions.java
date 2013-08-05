/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.multicurve;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.multicurve.discounting.LinearFIDiscountingPV01Function;
import com.opengamma.financial.analytics.model.multicurve.discounting.LinearFIDiscountingPVFunction;
import com.opengamma.financial.analytics.model.multicurve.discounting.LinearFIDiscountingParRateFunction;
import com.opengamma.financial.analytics.model.multicurve.discounting.LinearFIDiscountingYCNSFunction;
import com.opengamma.financial.analytics.model.multicurve.hullwhite.LinearFixedIncomeHullWhitePVFunction;

/**
 *
 */
public class MulticurvePricingFunctions extends AbstractFunctionConfigurationBean {

  public static FunctionConfigurationSource instance() {
    return new MulticurvePricingFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(LinearFIDiscountingPVFunction.class));
    functions.add(functionConfiguration(LinearFIDiscountingParRateFunction.class));
    functions.add(functionConfiguration(LinearFIDiscountingPV01Function.class));
    functions.add(functionConfiguration(LinearFIDiscountingYCNSFunction.class));
    functions.add(functionConfiguration(LinearFixedIncomeHullWhitePVFunction.class));
  }
}
