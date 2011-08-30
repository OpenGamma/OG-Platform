/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

import com.opengamma.web.server.conversion.ConversionMode;

import java.util.Collections;
import java.util.Map;

/**
 *
 */
public interface Viewport {

  // TODO this should be a proper class but that would require serious refactoring of the web code
  Map<String, Object> getGridStructure();

  // TODO this should be a proper class but that would require serious refactoring of the web code
  Map<String, Object> getLatestResults();

  void setRunning(boolean run);

  void setConversionMode(ConversionMode mode);

  void close();
}
