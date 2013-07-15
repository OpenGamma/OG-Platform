/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

/**
 * Managed staged data for ISDA test cases
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 */
public class ISDAStagedDataManager {

  private static final Pattern TEST_GRID_REGEX = Pattern.compile("([A-Z]{3}+)_([0-9]{8}+)\\.xls", Pattern.CASE_INSENSITIVE);
  private static final DateTimeFormatter GRID_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
  private static final DateTimeFormatter STAGED_DATE_FORMAT = DateTimeFormatter.ofPattern("dd_MM_yyyy");
  
  private static final String RESOURCE_DIR = "resources";
  private static final String STAGED_CURVE_DIR = "isda_staged_curves";
  private static final String STAGED_CURVE_BASENAME = "IsdaIrCurve_";

  public ISDAStagedCurve loadStagedCurveForGrid(final String gridFilename) throws IOException, JAXBException {

    InputStream is = null;

    try {

      final Matcher matcher = TEST_GRID_REGEX.matcher(gridFilename);

      if (!matcher.matches()) {
        return null;
      }

      final LocalDate testDate = LocalDate.parse(matcher.group(2), GRID_DATE_FORMAT);
      final String path = RESOURCE_DIR + File.separator + STAGED_CURVE_DIR + File.separator
          + STAGED_CURVE_BASENAME + matcher.group(1) + "_" + testDate.toString(STAGED_DATE_FORMAT) + ".xml";
      is = getClass().getClassLoader().getResourceAsStream(path);

      if (is == null) {
        return null;
      }

      final JAXBContext jaxbContext = JAXBContext.newInstance(ISDAStagedCurve.class);
      final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (ISDAStagedCurve) jaxbUnmarshaller.unmarshal(is);
    } finally {
      if (is != null) {
        is.close();
      }
    }
  }

}
