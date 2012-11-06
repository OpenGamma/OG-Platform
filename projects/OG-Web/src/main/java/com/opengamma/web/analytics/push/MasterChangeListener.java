/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.push;

import com.opengamma.web.analytics.rest.MasterType;

/**
 * Listener that is notified when any data changes in a master.
 */
/* package */ interface MasterChangeListener {

  /**
   * Invoked when any data changes in a master.
   * 
   * @param masterType  the type of the master whose data has changed, not null
   */
  void masterChanged(MasterType masterType);

}
