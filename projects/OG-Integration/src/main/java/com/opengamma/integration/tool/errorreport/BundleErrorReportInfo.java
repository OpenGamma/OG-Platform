/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.errorreport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.integration.tool.GUIFeedback;
import com.opengamma.util.ArgumentChecker;

/**
 * Gathers up all of the logs from all installed locations and prepares a ZIP file that can be submitted to the OpenGamma developers as part of a bug report.
 */
public class BundleErrorReportInfo implements Runnable {

  private static final Logger s_logger = LoggerFactory.getLogger(BundleErrorReportInfo.class);
  private static final String[] s_subdirs = new String[] {"Downloads" };
  private static String s_userHome;

  protected static void setUserHome(final String userHome) {
    s_userHome = userHome;
  }

  private final GUIFeedback _feedback;
  private final String[] _reports;

  private int _uniqueFile;
  private ZipOutputStream _zip;

  /**
   * Reads the contents of a file. Any lines are trimmed of leading/trailing whitespace. Any lines starting with # are skipped and any blank lines ignored.
   * 
   * @param pathToFile the file to read, not null
   * @return the contents of the file, not null
   */
  private static String[] readFile(final String pathToFile) {
    try (final BufferedReader reader = new BufferedReader(new FileReader(pathToFile))) {
      final List<String> lines = new ArrayList<String>();
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (!line.startsWith("#")) {
          lines.add(line);
        }
      }
      return lines.toArray(new String[lines.size()]);
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Couldn't read properties file - " + pathToFile, e);
    }
  }

  protected BundleErrorReportInfo(final GUIFeedback feedback, final String pathToProperties) {
    this(feedback, readFile(pathToProperties));
  }

  protected BundleErrorReportInfo(final GUIFeedback feedback, final String[] properties) {
    _feedback = ArgumentChecker.notNull(feedback, "feedback");
    _reports = ArgumentChecker.notNull(properties, "properties");
  }

  /**
   * Find a preferred sub-directory under the user's home folder. We use the home folder as it is most likely writeable, but most systems have sub-folders called things like "Downloads" or
   * "Downloaded Files" that the user might prefer things end up in.
   * 
   * @param path path the check, not null
   * @return the updated path, or the original if no candidate sub-folder is found
   */
  private String preferredSubFolder(final String path) {
    final File file = new File(path);
    if (file.isDirectory()) {
      for (String subdir : s_subdirs) {
        final File sd = new File(file, subdir);
        if (sd.isDirectory()) {
          return path + File.separator + subdir;
        }
      }
    }
    return path;
  }

  /**
   * Opens the ZIP output stream ready for each "report" entry to be added.
   * 
   * @return the path that will be written to, not null
   */
  protected String openReportOutput() {
    final String home = preferredSubFolder((s_userHome != null) ? s_userHome : System.getProperty("user.home"));
    final LocalDateTime ldt = Instant.now().atZone(ZoneOffset.systemDefault()).toLocalDateTime();
    final String path = String.format("%s%c%s-%04d-%02d-%02d-%02d-%02d-%02d.zip", home, File.separatorChar, "OpenGamma-ErrorReport", ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth(),
        ldt.getHour(), ldt.getMinute(), ldt.getSecond()).toString();
    s_logger.info("Writing {}", path);
    try {
      _zip = new ZipOutputStream(new FileOutputStream(path));
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Couldn't write to " + path, e);
    }
    return path;
  }

  private String createUniqueName(final String name) {
    return (++_uniqueFile) + "-" + name;
  }

  /**
   * Copies a file from the local file system to the ZIP output.
   * 
   * @param source the file to copy from, not null
   * @param name the name of the entry, not null
   */
  protected void attachFile(final File source, final String name) {
    try {
      final ZipEntry ze = new ZipEntry(name);
      _zip.putNextEntry(ze);
      final byte[] buffer = new byte[4096];
      try (final FileInputStream in = new FileInputStream(source)) {
        int bytes;
        while ((bytes = in.read(buffer)) > 0) {
          _zip.write(buffer, 0, bytes);
        }
      }
      _zip.closeEntry();
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Couldn't write " + name + " to ZIP file", e);
    }
  }

  /**
   * Creates a regex pattern that corresponds to wild cards written with * and ? notation.
   * 
   * @param name the * and ? based pattern
   * @return the regex pattern
   */
  private Pattern fileNameMatch(final String name) {
    final StringBuilder sb = new StringBuilder(name.length());
    for (int i = 0; i < name.length(); i++) {
      final char c = name.charAt(i);
      if (c == '.') {
        sb.append('\\').append('.');
      } else if (c == '?') {
        sb.append('.');
      } else if (c == '*') {
        sb.append('.').append('*').append('?');
      } else {
        sb.append(c);
      }
    }
    return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
  }

  private int attachFiles(final File root, final String path) {
    final int i = path.indexOf(File.separatorChar);
    final Pattern match;
    final String tail;
    if (i < 0) {
      match = fileNameMatch(path);
      tail = null;
    } else {
      match = fileNameMatch(path.substring(0, i));
      int tailStart = i;
      do {
        tailStart++;
        if (tailStart >= path.length()) {
          return 0;
        }
      } while (path.charAt(tailStart) == File.separatorChar);
      tail = path.substring(tailStart);
    }
    int count = 0;
    final String[] files = root.list();
    if (files != null) {
      Arrays.sort(files);
      for (String file : files) {
        final Matcher m = match.matcher(file);
        if (m.matches()) {
          s_logger.trace("Entry {} matched by path", file);
          final File entry = new File(root, file);
          if (tail != null) {
            if (entry.isDirectory()) {
              count += attachFiles(entry, tail);
            }
          } else {
            if (entry.isFile()) {
              s_logger.info("Attaching {}", entry);
              attachFile(entry, createUniqueName(file));
              count++;
            }
          }
        }
      }
    }
    return count;
  }

  /**
   * Writes an entry of the form "AttachFiles=&lt;path&gt;".
   * 
   * @param path the path, including * and ? characters as wildcards
   * @return the number of files written to the ZIP file
   */
  private int attachFiles(String path) {
    if (path.startsWith("%TEMP%")) {
      final String tmpdir = System.getProperty("java.io.tmpdir");
      s_logger.trace("Substituting " + tmpdir + " for %TEMP%");
      path = tmpdir + path.substring(6);
    }
    s_logger.debug("Attaching {}", path);
    final int i = path.indexOf(File.separatorChar);
    if (i < 0) {
      throw new IllegalArgumentException("File path '" + path + "' is not a valid absolute path");
    }
    final File root = new File(path.substring(0, i + 1));
    return attachFiles(root, path.substring(i + 1));
  }

  /**
   * Writes an entry of the form "X=Y", dispatching the call based on the value of X.
   * 
   * @param report the line from the configuration file, not null
   * @return the number of files written to the ZIP file
   */
  private int writeReport(String report) {
    s_logger.debug("Writing {}", report);
    final int i = report.indexOf('=');
    if (i < 0) {
      s_logger.warn("Error in configuration - {}", report);
      return 0;
    }
    final String key = report.substring(0, i).trim();
    final String value = report.substring(i + 1).trim();
    s_logger.trace("Key = \"{}\", Value = \"{}\"", key, value);
    if ("AttachFiles".equalsIgnoreCase(key)) {
      return attachFiles(value);
    } else {
      s_logger.warn("Unrecognised option - {}", key);
      return 0;
    }
  }

  /**
   * Closes the file opened by {@link #openReportOutput()}.
   */
  protected void closeReportOutput() {
    try {
      _zip.close();
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Couldn't write to ZIP file", e);
    }
  }

  @Override
  public void run() {
    _feedback.workRequired(_reports.length + 2);
    final String pathToReport = openReportOutput();
    _feedback.workCompleted(1);
    int reportCount = 0;
    for (String report : _reports) {
      reportCount += writeReport(report);
      _feedback.workCompleted(1);
    }
    closeReportOutput();
    _feedback.workCompleted(1);
    _feedback.done(reportCount + " log(s) written to " + pathToReport);
  }

  /**
   * Logical program entry point.
   * 
   * @param args the command line arguments, not null
   * @return the exit code
   */
  protected static int mainImpl(final String[] args) {
    final GUIFeedback feedback = new GUIFeedback("Packaging error logs for submission");
    try {
      if (args.length != 1) {
        throw new IllegalArgumentException("Invalid number of arguments - expected path to property file");
      }
      (new BundleErrorReportInfo(feedback, args[0])).run();
      return 0;
    } catch (Throwable t) {
      s_logger.error("Caught exception", t);
      feedback.shout((t.getMessage() != null) ? t.getMessage() : "Couldn't package error logs");
      return 1;
    }
  }

  /**
   * Program entry point. The logical program entry point is called and {@link System#exit} called with the exit code.
   * 
   * @param args the command line arguments, not null
   */
  public static void main(final String[] args) {
    System.exit(mainImpl(args));
  }

}
