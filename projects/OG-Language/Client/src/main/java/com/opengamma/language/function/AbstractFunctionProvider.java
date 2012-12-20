/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import com.opengamma.language.definition.AbstractDefinitionProvider;

/**
 * Partial {@link FunctionProvider} implementation that implements caching.
 */
public abstract class AbstractFunctionProvider extends AbstractDefinitionProvider<MetaFunction> implements
    FunctionProvider {

  protected AbstractFunctionProvider() {
    super();
  }

}
