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
  
  InputStream openConnection();

}
