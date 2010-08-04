/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 *
 */
public class MissingInputException extends OpenGammaRuntimeException {
  private static final String MESSAGE_FORMAT =
    "Unable to load input {0} for invocation on function {1}";
  
  private final Collection<ValueSpecification> _missingInputs;
  
  public MissingInputException(Collection<ValueSpecification> missingInputs, String functionUniqueIdentifier) {
    super(MessageFormat.format(MESSAGE_FORMAT, missingInputs, functionUniqueIdentifier));
    _missingInputs = new HashSet<ValueSpecification>(missingInputs);
  }

  public Collection<ValueSpecification> getMissingInputs() {
    return Collections.unmodifiableCollection(_missingInputs);
  }
  
}
