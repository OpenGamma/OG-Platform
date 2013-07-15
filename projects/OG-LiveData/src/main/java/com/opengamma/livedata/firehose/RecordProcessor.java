/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.firehose;

/**
 * A simple interface that allows for processing of a particular record.
 * This interface is primarily useful for dispatching after a record
 * has been accepted.
 * 
 * @param <TRecord> The type of the record to process.
 */
public interface RecordProcessor<TRecord> {

  /**
   * Process the record.
   * @param record The record to process
   */
  void process(TRecord record);
}
