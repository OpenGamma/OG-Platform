/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.install.msi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Modify an AIP file to include the default parameters for the MSMs.
 */
public class MergeModDefaultParameters implements Runnable {

  private static final Pattern s_msmModule = Pattern.compile("\\s*<row.*?module=\"([^\"]*)\".*", Pattern.CASE_INSENSITIVE);
  private static final Pattern s_msmPath = Pattern.compile("\\s*<row.*?path=\"([^\"]*)\".*", Pattern.CASE_INSENSITIVE);
  private static final Pattern s_msmParams = Pattern.compile("\\s*<row.*?params=\"([^\"]*)\".*", Pattern.CASE_INSENSITIVE);

  private static enum Section {
    UNKNOWN,
    MERGE_MODULES
  }

  private final File _aipFile;

  public MergeModDefaultParameters(final File aipFile) {
    _aipFile = aipFile;
  }

  private File getAipFile() {
    return _aipFile;
  }

  private File createTempFile() {
    try {
      return File.createTempFile("installer", ".aip");
    } catch (IOException ex) {
      throw new RuntimeException("Couldn't create temporary file", ex);
    }
  }

  private String mergeModuleParams(final String msmPath) {
    if (!new File(msmPath).exists()) {
      System.err.println("MSM " + msmPath + " not found");
      return null;
    }
    synchronized (MergeModDefaultParameters.class) {
      final String tmpDir = System.getProperty("java.io.tmpdir");
      try {
        final Process msidb = Runtime.getRuntime().exec(new String[] {"msidb", "-d" + msmPath, "-f" + tmpDir, "-eModuleConfiguration" });
        if (msidb.waitFor() != 0) {
          System.out.println("No module configuration table in " + msmPath);
          return null;
        }
      } catch (Exception ex) {
        throw new RuntimeException("Error running msidb", ex);
      }
      try {
        final BufferedReader br = new BufferedReader(new FileReader(tmpDir + "\\ModuleConfiguration.idt"));
        br.readLine();
        br.readLine();
        br.readLine();
        String line;
        final StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
          final String[] elements = line.split("\t");
          if (elements.length > 4) {
            sb.append(elements[0]).append("=[").append(elements[0]).append("];");
          }
        }
        System.out.println("Found " + sb + " for " + msmPath);
        return sb.toString();
      } catch (IOException ex) {
        throw new RuntimeException("Error reading module configuration table", ex);
      }
    }
  }

  private String mergeModuleLine(final String line) {
    final Matcher msmModule = s_msmModule.matcher(line);
    if (!msmModule.matches()) {
      return line;
    }
    final Matcher msmPath = s_msmPath.matcher(line);
    if (!msmPath.matches()) {
      return line;
    }
    final String params = mergeModuleParams(msmPath.group(1));
    if (params == null) {
      return line;
    }
    final Matcher msmParams = s_msmParams.matcher(line);
    if (msmParams.matches()) {
      if (!params.equals(msmParams.group(1))) {
        throw new IllegalStateException("Assumed parameters - " + params + " different to " + line);
      }
      return line;
    } else {
      return line.replace("/>", " Params=\"" + params + "\"/>");
    }
  }

  private void processFile(final File source, final File dest) {
    try {
      final BufferedReader br = new BufferedReader(new FileReader(source));
      try {
        final Writer w = new BufferedWriter(new FileWriter(dest));
        try {
          String line;
          Section section = Section.UNKNOWN;
          while ((line = br.readLine()) != null) {
            if (line.contains("<COMPONENT")) {
              if (line.contains("MsiMergeModsComponent")) {
                section = Section.MERGE_MODULES;
              } else {
                section = Section.UNKNOWN;
              }
            } else {
              switch (section) {
                case MERGE_MODULES:
                  line = mergeModuleLine(line);
                  break;
              }
            }
            w.write(line);
            w.write("\r\n");
          }
        } finally {
          w.close();
        }
      } finally {
        br.close();
      }
    } catch (IOException ex) {
      throw new RuntimeException("Error reading/writing file", ex);
    }
  }

  private void renameTempFile(final File tmp) {
    if (!getAipFile().delete()) {
      throw new RuntimeException("Couldn't delete " + getAipFile() + " before renaming temporary file");
    }
    if (!tmp.renameTo(getAipFile())) {
      throw new RuntimeException("Couldn't rename " + tmp + " to " + getAipFile());
    }
  }

  @Override
  public void run() {
    final File tmp = createTempFile();
    try {
      processFile(getAipFile(), tmp);
      renameTempFile(tmp);
    } finally {
      tmp.delete();
    }
  }

  public static void main(final String[] args) {
    for (String arg : args) {
      new MergeModDefaultParameters(new File(arg)).run();
    }
  }

}
