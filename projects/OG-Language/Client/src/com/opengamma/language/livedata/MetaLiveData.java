/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.livedata;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A full description of a live data element, including invocation details. 
 */
public class MetaLiveData extends Definition {

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

  public MetaLiveData(final String name) {
    super(name);
  }

  // TODO: invocation details
  // TODO: details of parameters wrt Java type system
  // TODO: details of type wrt Java type system

}
