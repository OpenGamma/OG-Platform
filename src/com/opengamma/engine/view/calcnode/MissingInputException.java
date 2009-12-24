/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.text.MessageFormat;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.AnalyticValueDefinition;

/**
 * 
 *
 * @author kirk
 */
public class MissingInputException extends OpenGammaRuntimeException {
  private static final String MESSAGE_FORMAT =
    "Unable to load input {0} for invocation on function {1}";
  
  public MissingInputException(AnalyticValueDefinition<?> definition, String functionUniqueIdentifier) {
    super(MessageFormat.format(MESSAGE_FORMAT, definition, functionUniqueIdentifier));
  }

}
