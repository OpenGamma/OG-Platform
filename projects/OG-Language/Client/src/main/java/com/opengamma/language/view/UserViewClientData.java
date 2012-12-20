/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

/**
 * Language specific data extension to {@link UserViewClient}.
 */
public abstract class UserViewClientData {

  /**
   * Called when the {@link UserViewClient} wrapper gets destroyed, before the {@link ViewClient} is shut down.
   */
  protected void destroy() {
    // No-op
  }

}
