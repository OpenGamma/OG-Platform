/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import java.util.Collection;

import com.opengamma.language.function.AbstractFunctionProvider;
import com.opengamma.language.function.MetaFunction;

/**
 * Supplies the configuration item functions.
 */
public class ConfigurationFunctionProvider extends AbstractFunctionProvider {

  @Override
  protected void loadDefinitions(final Collection<MetaFunction> definitions) {
    definitions.add(MarketDataOverrideFunction.INSTANCE.getMetaFunction());
    definitions.add(ValuePropertyFunction.INSTANCE.getMetaFunction());
    definitions.add(ViewCalculationRateFunction.INSTANCE.getMetaFunction());
  }

}
