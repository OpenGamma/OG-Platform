/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.livedata.firehose;

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstraction of the stream decoder. The input stream is processed and discrete records returned.
 * 
 * @param <Record> the record representation
 */
public interface RecordStream<Record> {

  /**
   * Factory interface for constructing new record streams.
   * 
   * @param <Record> the record representation
   */
  interface Factory<Record> {

    RecordStream<Record> newInstance(InputStream input);

  }

  Record readRecord() throws IOException;

}
