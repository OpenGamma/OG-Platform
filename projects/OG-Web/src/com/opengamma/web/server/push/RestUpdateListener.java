/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import java.util.Collection;

/**
 *
 */
/* package */ interface RestUpdateListener {

  void itemUpdated(String url);

  void itemsUpdated(Collection<String> url);
}
