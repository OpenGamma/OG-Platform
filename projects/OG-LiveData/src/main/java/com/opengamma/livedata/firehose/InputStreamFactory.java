/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.firehose;

import java.io.InputStream;

// NOTE kirk 2013-03-19 -- This is a candidate for promotion to OG-Util
// if other things other than Cogda find a use for it.
/**
 * A simple interface that can establish an {@link InputStream} on demand.
 */
public interface InputStreamFactory {
  
  /**
   * Open a connection.
   * In the case that a connection cannot be made, this method <em>must</em>
   * throw an unchecked exception rather than returning null.
   * @return A valid connection.
   */
  InputStream openConnection();
  
  /**
   * Obtain a description of the underlying connection.
   * For example, if the factory is backed by a file, it might return
   * the file name. If it's based on a socket, it might describe the connection
   * parameters.
   * @return A description for the factory.
   */
  String getDescription();

}
