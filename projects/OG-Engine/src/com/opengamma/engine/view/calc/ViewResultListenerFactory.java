/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.listener.ViewResultListener;

/**
 * Factory producing sticky view clients. These clients do not detach themeselves from view processes.
 * The use case for this kind of clients is to use them as additional clients receiving all the results as the oryginalone,
 * which keeps processing results coming from view process even when the original view client detaches between computation cycels.
 * Useful for dumping all view process results on disk while not being attached by the invoking client.
 *
 */
public interface ViewResultListenerFactory {

  ViewResultListener createViewResultListener();
}
