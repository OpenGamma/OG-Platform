/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.util.ArgumentChecker;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Produce a {@link FunctionProvider} from a set of {@link PublishedFunction} objects.
 */
public class FunctionProviderBean extends AbstractFunctionProvider implements InitializingBean {
  
  private Collection<PublishedFunction> _functions;

  public void setFunctions(final Collection<PublishedFunction> functions) {
    ArgumentChecker.notNull(functions, "functions");
    _functions = new ArrayList<PublishedFunction>(functions);
  }

  private Collection<PublishedFunction> getFunctionsInternal() {
    return _functions;
  }

  @SuppressWarnings("unchecked")
  public Collection<PublishedFunction> getFunctions() {
    return Collections.unmodifiableCollection(getFunctionsInternal());
  }

  // InitializingBean

  @Override
  public void afterPropertiesSet() {
    ArgumentChecker.notNull(getFunctionsInternal(), "functions");
  }

  // AbstractFunctionProvider

  @Override
  protected void loadDefinitions(final Collection<MetaFunction> definitions) {
    for (PublishedFunction function : getFunctionsInternal()) {
      definitions.add(function.getMetaFunction());
    }
  }

}
