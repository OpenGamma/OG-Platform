/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Set;

import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.util.PublicAPI;

/**
 * A view processor manages the computation of views, and is responsible for initializing the computation context of
 * these views. A view processor can manage only one instance of any view, but potentially there can be many view
 * processors responsible for possibly-overlapping sets of views.
 * <p>
 * This interface exposes full control over the view processor and is designed for internal use by the engine.
 * The {@link ViewProcessorClient} interface is designed to provide more restricted access to a {@link ViewProcessor}
 * for external use.
 */
@PublicAPI
public interface ViewProcessor {

  /**
   * Gets the names of the views which the view processor can provide access to. Not all of these views are necessarily
   * initialized, less so being processed.
   * 
   * @return  a set of view names
   */
  Set<String> getViewNames();
  
  /**
   * Obtains a {@link View} instance.
   * 
   * @param name  the name of the view to obtain, not null
   * @param credentials  the user attempting to access the view, not null
   * @return  the view
   */
  View getView(String name, UserPrincipal credentials);

}
