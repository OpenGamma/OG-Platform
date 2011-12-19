package com.opengamma.web.server.push;

import com.opengamma.web.server.push.rest.MasterType;

/**
 * Listener that is notified when any data changes in a master.
 */
/* package */ interface MasterChangeListener {

  /**
   * Invoked when any data changes in a master.
   * @param masterType The type of the master whose data has changed
   */
  void masterChanged(MasterType masterType);
}
