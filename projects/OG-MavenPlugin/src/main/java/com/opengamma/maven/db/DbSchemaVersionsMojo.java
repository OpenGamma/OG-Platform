/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maven.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.opengamma.util.db.script.DbScriptDirectory;
import com.opengamma.util.db.script.DbScriptReader;
import com.opengamma.util.db.script.FileDbScriptDirectory;
import com.opengamma.util.db.tool.DbSchemaVersionUtils;

/**
 * Maven plugin to generate database schema version files from a set of database installation scripts.
 * 
 * @goal db-schema-versions
 */
public class DbSchemaVersionsMojo extends AbstractMojo {
  
  /**
   * @parameter alias="scriptsPath"
   * @required
   */
  private String _scriptsPath;
  /**
   * @parameter alias="outputPath"
   * @required
   */
  private String _outputPath;
  
  //-------------------------------------------------------------------------
  public String getScriptsPath() {
    return _scriptsPath;
  }
  
  public String getOutputPath() {
    return _outputPath;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    final File scriptsDir = new File(getScriptsPath());
    if (!scriptsDir.exists() || !scriptsDir.isDirectory()) {
      throw new MojoExecutionException("scriptsPath must be an existing directory");
    }
    final File outputDir = new File(getOutputPath());
    if (outputDir.isFile()) {
      throw new MojoExecutionException("outputPath must be a directory");
    }
    if (!outputDir.exists()) {
      outputDir.mkdirs();
    }
    DbScriptDirectory baseDir = new FileDbScriptDirectory(scriptsDir);
    DbScriptReader scriptReader = new DbScriptReader(baseDir);
    Map<String, Integer> latestVersions = scriptReader.getLatestVersions();
    for (Map.Entry<String, Integer> entry : latestVersions.entrySet()) {
      String schemaName = entry.getKey();
      int version = entry.getValue();
      try {
        final File f = new File(outputDir, DbSchemaVersionUtils.getSchemaVersionFileName(schemaName));
        if (f.exists()) {
          f.delete();
        }
        f.createNewFile();
        final FileOutputStream fos = new FileOutputStream(f);
        final OutputStreamWriter out = new OutputStreamWriter(fos);
        try {
          out.write(Integer.toString(version));
        } finally {
          out.close();
        }
        getLog().info("Database schema '" + schemaName + "' at version " + version);
      } catch (Exception e) {
        throw new MojoExecutionException("Error writing file for schema '" + schemaName + "'", e);
      }
    }
  }

}
