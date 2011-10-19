/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A full description of a parameter including Java type system data. 
 */
public class MetaParameter extends Parameter {

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

  private final JavaTypeInfo<?> _javaTypeInfo;

  public MetaParameter(final String name, final JavaTypeInfo<?> javaTypeInfo) {
    super(name, !javaTypeInfo.isAllowNull() && !javaTypeInfo.isDefaultValue());
    _javaTypeInfo = javaTypeInfo;
  }

  protected MetaParameter(final MetaParameter copyFrom) {
    super(copyFrom);
    _javaTypeInfo = copyFrom.getJavaTypeInfo();
  }

  public MetaParameter description(final String description) {
    super.setDescription(description);
    return this;
  }

  public JavaTypeInfo<?> getJavaTypeInfo() {
    return _javaTypeInfo;
  }

  @Override
  public MetaParameter clone() {
    return new MetaParameter(this);
  }

}
