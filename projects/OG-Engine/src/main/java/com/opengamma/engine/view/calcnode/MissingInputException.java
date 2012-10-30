/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class MissingInputException extends OpenGammaRuntimeException {

  private static final long serialVersionUID = 1L;

  private static final String MESSAGE_FORMAT =
    "Unable to load input {0} for invocation on function {1}";
  
  private final Set<ValueSpecification> _missingInputs;
  
  public MissingInputException(Collection<ValueSpecification> missingInputs, String functionUniqueIdentifier) {
    super(MessageFormat.format(MESSAGE_FORMAT, missingInputs, functionUniqueIdentifier));
    _missingInputs = new HashSet<ValueSpecification>(missingInputs);
  }

  public Set<ValueSpecification> getMissingInputs() {
    return Collections.unmodifiableSet(_missingInputs);
  }
  
}
