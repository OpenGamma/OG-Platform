/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.view;

import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableUserContext;

/**
 * Extends the user context with view processor support (if available).
 */
public class UserLoader extends ContextInitializationBean {

  // ContextInitializationBean

  @Override
  protected void initContext(final MutableUserContext userContext) {
    userContext.setViewClients(new ViewClients(userContext));
  }

  @Override
  protected void doneContext(final MutableUserContext userContext) {
    userContext.getViewClients().destroyAll();
  }

}
