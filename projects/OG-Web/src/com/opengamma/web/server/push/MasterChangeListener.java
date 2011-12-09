package com.opengamma.web.server.push;

import com.opengamma.web.server.push.rest.MasterType;

/**
 *
 */
/* package */ interface MasterChangeListener {

  void masterChanged(MasterType masterType);
}
