/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.opengamma.util.ArgumentChecker;

/**
 * A full description of a function, including invocation details. 
 */
public class MetaFunction extends Definition {

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

  private final FunctionInvoker _invoker;

  public MetaFunction(final String name, final FunctionInvoker invoker) {
    super(name);
    ArgumentChecker.notNull(invoker, "invoker");
    _invoker = invoker;
  }

  public FunctionInvoker getInvoker() {
    return _invoker;
  }

  // TODO: details of parameters wrt Java type system
  // TODO: details of return wrt Java type system

}
