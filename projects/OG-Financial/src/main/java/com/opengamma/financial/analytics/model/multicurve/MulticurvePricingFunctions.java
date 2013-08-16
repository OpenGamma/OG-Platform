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
import com.opengamma.financial.analytics.model.discounting.DiscountingBCSFunction;
import com.opengamma.financial.analytics.model.discounting.DiscountingCurrencyExposureFunction;
import com.opengamma.financial.analytics.model.discounting.DiscountingFXPVFunction;
import com.opengamma.financial.analytics.model.discounting.DiscountingPV01Function;
import com.opengamma.financial.analytics.model.discounting.DiscountingPVFunction;
import com.opengamma.financial.analytics.model.discounting.DiscountingParRateFunction;
import com.opengamma.financial.analytics.model.discounting.DiscountingYCNSFunction;
import com.opengamma.financial.analytics.model.hullwhitediscounting.HullWhiteBCSFunction;
import com.opengamma.financial.analytics.model.hullwhitediscounting.HullWhitePV01Function;
import com.opengamma.financial.analytics.model.hullwhitediscounting.HullWhitePVFunction;
import com.opengamma.financial.analytics.model.hullwhitediscounting.HullWhiteParRateFunction;
import com.opengamma.financial.analytics.model.hullwhitediscounting.HullWhiteYCNSFunction;

/**
 *
 */
public class MulticurvePricingFunctions extends AbstractFunctionConfigurationBean {

  public static FunctionConfigurationSource instance() {
    return new MulticurvePricingFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(DiscountingBCSFunction.class));
    functions.add(functionConfiguration(DiscountingCurrencyExposureFunction.class));
    functions.add(functionConfiguration(DiscountingFXPVFunction.class));
    functions.add(functionConfiguration(DiscountingPVFunction.class));
    functions.add(functionConfiguration(DiscountingParRateFunction.class));
    functions.add(functionConfiguration(DiscountingPV01Function.class));
    functions.add(functionConfiguration(DiscountingYCNSFunction.class));
    functions.add(functionConfiguration(HullWhiteBCSFunction.class));
    functions.add(functionConfiguration(HullWhiteParRateFunction.class));
    functions.add(functionConfiguration(HullWhitePVFunction.class));
    functions.add(functionConfiguration(HullWhitePV01Function.class));
    functions.add(functionConfiguration(HullWhiteYCNSFunction.class));
  }
}
