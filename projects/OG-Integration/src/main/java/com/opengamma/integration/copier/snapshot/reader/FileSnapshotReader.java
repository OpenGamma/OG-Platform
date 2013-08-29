/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.reader;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.core.marketdatasnapshot.CurveKey;
import com.opengamma.core.marketdatasnapshot.CurveSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableCurveSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.integration.copier.sheet.reader.CsvSheetReader;
import com.opengamma.integration.copier.snapshot.SnapshotColumns;
import com.opengamma.integration.copier.snapshot.SnapshotType;

/**
 * Reads a snapshot from an imported file
 */
public class FileSnapshotReader implements SnapshotReader {

  private static final Logger s_logger = LoggerFactory.getLogger(FileSnapshotReader.class);

  private CsvSheetReader _sheetReader;
  private Enumeration<ZipEntry> _sheetEntries;
  private Map<CurveKey, CurveSnapshot> _curves;
  private UnstructuredMarketDataSnapshot _global;
  private Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> _surface;
  private Map<YieldCurveKey, YieldCurveSnapshot> _yieldCurve;
  private String _name;
  private String _basisName;

  public FileSnapshotReader(String filename) {
    _sheetReader = new CsvSheetReader(filename);
    iterateSheetRows();
  }

  private void iterateSheetRows() {

    _curves = new HashMap<>();

    //Temporary maps for data structures
    HashMap<String, ManageableCurveSnapshot> curvesBuilder = new HashMap<>();

    while (true) {
      Map<String, String> currentRow = _sheetReader.loadNextRow();

      // When rows are complete create snapshot elements from temporary structures
      if (currentRow == null) {
        for (Map.Entry<String, ManageableCurveSnapshot> entry : curvesBuilder.entrySet()) {
          _curves.put(new CurveKey(entry.getKey()), entry.getValue());
        }
        return;
      }

      String type = currentRow.get(SnapshotColumns.TYPE.get());

      switch (SnapshotType.from(type)) {
        case NAME:
          _name = currentRow.get(SnapshotColumns.NAME.get());
          break;
        case BASIS_NAME:
          _basisName = currentRow.get(SnapshotColumns.NAME.get());
          break;
        case CURVE:
          buildCurves(curvesBuilder, currentRow);
          break;
        case YIELD_CURVE:
          break;
        case GOBAL_VALUES:
          break;
        case VOL_SURFACE:
          break;
        default:
          s_logger.error("Unknown snapshot element of type {}", type);
          break;
      }
    }
  }

  private void buildCurves(HashMap<String, ManageableCurveSnapshot> curvesBuilder, Map<String, String> currentRow) {
    String name = currentRow.get(SnapshotColumns.NAME.get());
    if (!curvesBuilder.containsKey(name)) {
      ManageableCurveSnapshot curve = new ManageableCurveSnapshot();
      ManageableUnstructuredMarketDataSnapshot snapshot = new ManageableUnstructuredMarketDataSnapshot();

      curve.setValuationTime(Instant.parse(currentRow.get(SnapshotColumns.INSTANT.get())));
      snapshot.putValue(createExternalIdBundle(currentRow),
                        currentRow.get(SnapshotColumns.VALUE_NAME.get()),
                        createValueSnapshot(currentRow));
      curve.setValues(snapshot);
      curvesBuilder.put(name, curve);
    } else {
      curvesBuilder.get(name).getValues().putValue(createExternalIdBundle(currentRow),
                                                   currentRow.get(SnapshotColumns.VALUE_NAME.get()),
                                                   createValueSnapshot(currentRow));
    }

  }

  private ValueSnapshot createValueSnapshot(Map<String, String> currentRow) {
    String marketValue = currentRow.get(SnapshotColumns.MARKET_VALUE.get());
    String overrideValue = currentRow.get(SnapshotColumns.OVERRIDE_VALUE.get());
    return new ValueSnapshot(marketValue, overrideValue);
  }

  private ExternalIdBundle createExternalIdBundle(Map<String, String> currentRow) {
    Iterable<String> iterable = Arrays.asList(currentRow.get(SnapshotColumns.ID_BUNDLE.get()).split("\\|"));
    return ExternalIdBundle.parse(iterable);
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
    _sheetReader.close();
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
