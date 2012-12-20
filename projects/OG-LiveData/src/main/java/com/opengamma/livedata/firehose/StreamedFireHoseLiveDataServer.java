/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.livedata.firehose;

import com.opengamma.id.ExternalScheme;

/**
 * OpenGamma live data server implementation that works from a {@link StreamedFireHoseLiveData} implementation.
 * 
 * @param <T> the record type
 */
public class StreamedFireHoseLiveDataServer<T> extends FireHoseLiveDataServer {

  public StreamedFireHoseLiveDataServer(final ExternalScheme uniqueIdDomain, final StreamedFireHoseLiveData<T> fireHose) {
    super(uniqueIdDomain, fireHose);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected StreamedFireHoseLiveData<T> getFireHose() {
    return (StreamedFireHoseLiveData<T>) super.getFireHose();
  }

  public void setPipeLineIO(final boolean pipeLineIO) {
    getFireHose().setPipeLineIO(pipeLineIO);
  }

  public boolean isPipeLineIO() {
    return getFireHose().isPipeLineIO();
  }

  public void setConnectorFactory(final AbstractConnectorJob.Factory<T> factory) {
    getFireHose().setConnectorFactory(factory);
  }

  public AbstractConnectorJob.Factory<T> getConnectorFactory() {
    return getFireHose().getConnectorFactory();
  }

  public void setStreamFactory(final RecordStream.Factory<T> factory) {
    getFireHose().setStreamFactory(factory);
  }

  public RecordStream.Factory<T> getStreamFactory() {
    return getFireHose().getStreamFactory();
  }

}
