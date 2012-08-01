/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.List;

/**
 *
 */
public interface AnalyticsViewListener {

  void gridStructureChanged(String gridId);

  void gridStructureChanged(List<String> gridIds);

  // TODO columnStructureChanged? rowStructureChanged?

  void gridDataChanged(String dataId);

  void gridDataChanged(List<String> dataIds);
}
