/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.client;


/**
 * Enumeration for identifying masters from the three contexts.
 */
public enum MasterID {
    /**
     * The master from the session context's client.
     */
    SESSION(CombiningMaster.SESSION_MASTER_DISPLAY_NAME),
    /**
     * The master from the user context's client.
     */
    USER(CombiningMaster.USER_MASTER_DISPLAY_NAME),
    /**
     * The master from the global context's client.
     */
    GLOBAL(CombiningMaster.GLOBAL_MASTER_DISPLAY_NAME);

  private final String _label;

  private MasterID(final String label) {
    _label = label;
  }

  public String getLabel() {
    return _label;
  }

}
