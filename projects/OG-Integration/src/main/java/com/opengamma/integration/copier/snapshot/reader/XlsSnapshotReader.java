/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.reader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.CurveKey;
import com.opengamma.core.marketdatasnapshot.CurveSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;

/**
 * Reads a snapshot from an imported file
 */
public class XlsSnapshotReader implements SnapshotReader{
  private static final Logger s_logger = LoggerFactory.getLogger(XlsSnapshotReader.class);

  private Map<CurveKey, CurveSnapshot> _curves;
  private UnstructuredMarketDataSnapshot _global;
  private Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> _surface;
  private Map<YieldCurveKey, YieldCurveSnapshot> _yieldCurve;
  private String _name;
  private String _basisName;

  public XlsSnapshotReader(String filename) {
    InputStream fileInputStream = openFile(filename);

  }

  protected static InputStream openFile(String filename) {
    // Open input file for reading
    FileInputStream fileInputStream;
    try {
      fileInputStream = new FileInputStream(filename);
    } catch (FileNotFoundException ex) {
      throw new OpenGammaRuntimeException("Could not open file " + filename + " for reading, exiting immediately.");
    }

    return fileInputStream;
  }

  @Override
  public Map<CurveKey, CurveSnapshot> readCurves() {
    return _curves;
  }

  @Override
  public UnstructuredMarketDataSnapshot readGlobalValues() {
    return _global;
  }

  @Override
  public Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> readVolatilitySurfaces() {
    return _surface;
  }

  @Override
  public Map<YieldCurveKey, YieldCurveSnapshot> readYieldCurves() {
    return _yieldCurve;
  }

  @Override
  public void close() {
    //TODO
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public String getBasisViewName() {
    return _basisName;
  }
}
