/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.engine.ViewResultsSerializer;

/**
 * Utility class for the recording tests
 */
public class RecordingUtility {

  private RecordingUtility() {/*private constructor, everything is static*/}

  public static void generateOutput(Results results) throws IOException {
    ViewResultsSerializer serializer = new ViewResultsSerializer(results);
    File documentFile = new File("/integration_tests/temp.xml");
    FileOutputStream fos = new FileOutputStream(documentFile);
    serializer.serializeViewOutputs(fos);
    fos.close();
  }

}
