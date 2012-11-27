/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.identifier;

import java.util.Collection;

import com.opengamma.language.function.AbstractFunctionProvider;
import com.opengamma.language.function.MetaFunction;

/**
 * Supplies the identifier based functions.
 */
public class IdentifierFunctionProvider extends AbstractFunctionProvider {

  @Override
  protected void loadDefinitions(final Collection<MetaFunction> definitions) {
    definitions.add(ExternalIdBundleFunction.INSTANCE.getMetaFunction());
    definitions.add(PortfolioComponentIdentifiersFunction.INSTANCE.getMetaFunction());
  }

}
