/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add description
 */
public class SnapshotUtil {

  private static final Logger s_logger = LoggerFactory.getLogger(SnapshotUtil.class);

  public static Map<String, String> buildName(String name) {
    Map<String, String> row = new HashMap<>();
    if (name == null || name.isEmpty()) {
      s_logger.warn("Snapshot does not contain name.");
      return row; //TODO or null?
    }
    row.put(SnapshotColumns.TYPE.get(), SnapshotType.NAME.get());
    row.put(SnapshotColumns.NAME.get(), name);
    return row;
  }

  public static Map<String, String>  buildBasisViewName(String basisName) {
    Map<String, String> row = new HashMap<>();
    if (basisName == null || basisName.isEmpty()) {
      s_logger.warn("Snapshot does not contain basis name.");
      return row;
    }
    row.put(SnapshotColumns.TYPE.get(), SnapshotType.BASIS_NAME.get());
    row.put(SnapshotColumns.NAME.get(), basisName);
    return row;
  }

}
