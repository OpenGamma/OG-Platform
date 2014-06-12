/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import com.opengamma.engine.depgraph.impl.DependencyNodeImpl;

/**
 * 
 */
public class DependencyNodeFormatter {
  /**
   * The default width for each level of indentation in the output dependency graph.
   */
  public static final int DEFAULT_INDENT_SIZE = 2;
  private final int _indentSize;
  private final String _indentText;

  public DependencyNodeFormatter() {
    this(DEFAULT_INDENT_SIZE);
  }

  public DependencyNodeFormatter(int indentSize) {
    if (indentSize < 0) {
      throw new IllegalArgumentException("Indent size must not be negative.");
    }
    _indentSize = indentSize;
    _indentText = constructIndentText(indentSize);
  }

  private static String constructIndentText(int indentSize) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < indentSize; i++) {
      sb.append(' ');
    }
    return sb.toString();
  }

  /**
   * @return the indentSize
   */
  public int getIndentSize() {
    return _indentSize;
  }

  /**
   * @return the indentText
   */
  protected String getIndentText() {
    return _indentText;
  }

  public void format(PrintStream ps, DependencyNode node) {
    PrintWriter pw = new PrintWriter(ps);
    format(pw, node);
  }

  public void format(Writer w, DependencyNode node) {
    PrintWriter pw = new PrintWriter(w);
    format(pw, node);
  }

  public void format(PrintWriter pw, DependencyNode node) {
    format(pw, node, 0);
  }

  protected void format(PrintWriter pw, DependencyNode node, int indentLevel) {
    if (node == null) {
      return;
    }
    for (int i = 0; i < indentLevel; i++) {
      pw.print(getIndentText());
    }
    pw.print(node.toString());
    pw.print(" ");
    pw.print("producing ");
    pw.print(DependencyNodeImpl.getOutputValues(node));
    pw.println();
    final int count = node.getInputCount();
    for (int i = 0; i < count; i++) {
      final DependencyNode subNode = node.getInputNode(i);
      format(pw, subNode, indentLevel + 1);
    }
  }

  public static String toString(DependencyNode node) {
    DependencyNodeFormatter formatter = new DependencyNodeFormatter();
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    formatter.format(pw, node);
    return sw.toString();
  }

}
