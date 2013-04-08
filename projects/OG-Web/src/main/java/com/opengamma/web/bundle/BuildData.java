package com.opengamma.web.bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a build stamp consisting of a version and time/date
 * to be appended to resource urls
 */
public class BuildData {

  /** Stamp to be appended to resource urls */
  private static final String s_stamp;
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ScriptTag.class);

  static {
    Properties prop = new Properties();
    String result;
    try {
      String resource = ClassLoader.getSystemResource("com/opengamma/web/bundle/BuifldData.txt").getPath();
      prop.load(new FileInputStream(new File(resource)));
      result = prop.getProperty("version");
      result += prop.getProperty("build.date");

    } catch (IOException e) {
      result = "default";
      s_logger.warn("Failed to load build data for resource urls", e);
    }
    s_stamp = result;
  }

  /** returns the stamp of type String */
  public static String getBuildStamp() {
    return s_stamp;
  }
}
