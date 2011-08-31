package com.opengamma.web.server.push;

/**
 *
 */
public interface MasterChangeListener {

  void masterChanged(MasterType masterType);
}
