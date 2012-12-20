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
import java.util.List;

import com.opengamma.language.definition.Parameter;
import com.opengamma.util.ArgumentChecker;

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

  private final LiveDataConnector _connector;

  public MetaLiveData(final String category, final String name, final List<? extends Parameter> parameters, final LiveDataConnector connector) {
    super(name);
    ArgumentChecker.notNull(connector, "connector");
    _connector = connector;
    setCategory(category);
    setParameter(parameters);
  }

  protected MetaLiveData(final MetaLiveData copyFrom) {
    super(copyFrom);
    _connector = copyFrom.getConnector();
  }

  public MetaLiveData description(final String description) {
    super.setDescription(description);
    return this;
  }

  public LiveDataConnector getConnector() {
    return _connector;
  }

  @Override
  public MetaLiveData clone() {
    return new MetaLiveData(this);
  }

}
