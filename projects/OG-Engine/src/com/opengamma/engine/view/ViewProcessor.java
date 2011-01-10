/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Set;

import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.PublicAPI;

/**
 * A view processor manages a set of views and provides these with a common computation context, including the source
 * of positions, securities, live data and computation resources. Only one instance of any view can be managed by an
 * individual view processor, but potentially there can be many view processors responsible for possibly-overlapping
 * sets of views.
 */
@PublicAPI
public interface ViewProcessor {

  /**
   * Gets the names of the views to which the view processor can provide access. Not all of these views are necessarily
   * initialized, less so being processed.
   * 
   * @return a set of view names
   */
  Set<String> getViewNames();
  
  /**
   * Obtains a {@link View} instance.
   * 
   * @param name  the name of the view to obtain, not null
   * @param credentials  the user attempting to access the view, not null
   * @return the view
   */
  View getView(String name, UserPrincipal credentials);
  
  /**
   * Asynchronously reinitializes the view processor.
   */
  void reinitAsync();

}
