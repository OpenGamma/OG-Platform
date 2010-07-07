/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.io.File;

/**
 * Collection of file utils
 * REVIEW: jim 7-July-2009 - there's almost certainly something that I can do this, this shouldn't be in here long term anyway. 
 *                           File paths shouldn't be embedded in the code anyway.
 */
public class FileUtils {
  public static String getSharedDrivePrefix() {
    if (System.getProperty("os.name").contains("Windows")) {
      return "O:";
    } else {
      return File.separator + "ogdev";
    }
  }
}
