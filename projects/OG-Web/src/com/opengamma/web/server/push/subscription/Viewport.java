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

  /** For use instead of {@code null} when there isn't a viewport available */
  public static final Viewport DUMMY = new Viewport() {
    @Override
    public Map<String, Object> getGridStructure() {
      return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> getLatestResults() {
      return Collections.emptyMap();
    }

    @Override
    public void setRunning(boolean run) {
      // do nothing
    }

    @Override
    public void setConversionMode(ConversionMode mode) {
      // do nothing
    }
  };
}
