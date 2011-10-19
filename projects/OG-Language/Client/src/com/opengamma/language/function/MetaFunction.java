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
import java.util.List;

import com.opengamma.language.definition.Parameter;
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

  public MetaFunction(final String category, final String name, final List<? extends Parameter> parameters, final FunctionInvoker invoker) {
    super(name);
    ArgumentChecker.notNull(invoker, "invoker");
    _invoker = invoker;
    setCategory(category);
    setParameter(parameters);
  }

  public MetaFunction(final String category, final String name, final List<? extends Parameter> parameters, final FunctionInvoker invoker, final int returnCount) {
    this(category, name, parameters, invoker);
    setReturnCount(returnCount);
  }

  protected MetaFunction(final MetaFunction copyFrom) {
    super(copyFrom);
    _invoker = copyFrom.getInvoker();
  }

  public MetaFunction description(final String description) {
    super.setDescription(description);
    return this;
  }

  public FunctionInvoker getInvoker() {
    return _invoker;
  }

  @Override
  public MetaFunction clone() {
    return new MetaFunction(this);
  }

}
