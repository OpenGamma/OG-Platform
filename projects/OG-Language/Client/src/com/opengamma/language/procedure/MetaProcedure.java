/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.procedure;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.opengamma.language.definition.Parameter;

/**
 * A full description of a procedure, including invocation details. 
 */
public class MetaProcedure extends Definition {

  /**
   * Class is not serializable
   */
  private static final long serialVersionUID = 0L;

  private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    throw new NotSerializableException();
  }

  private void writeObject(ObjectOutputStream ois) throws IOException {
    throw new NotSerializableException();
  }

  public MetaProcedure(final String name, final List<? extends Parameter> parameters) {
    super(name);
    setParameter(parameters);
  }

  public MetaProcedure(final String name, final List<? extends Parameter> parameters, final int returnCount) {
    this(name, parameters);
    setReturnCount(returnCount);
  }

  // TODO: invocation details

}
