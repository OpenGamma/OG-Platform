package com.opengamma.web.server.push;

/**
 *
 */
/* package */ interface MasterChangeManager {

  void addChangeListener(MasterChangeListener listener);

  void removeChangeListener(MasterChangeListener listener);
}
