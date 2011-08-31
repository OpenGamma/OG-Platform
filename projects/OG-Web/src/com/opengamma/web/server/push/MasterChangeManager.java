package com.opengamma.web.server.push;

/**
 *
 */
public interface MasterChangeManager {

  void addChangeListener(MasterChangeListener listener);

  void removeChangeListener(MasterChangeListener listener);
}
