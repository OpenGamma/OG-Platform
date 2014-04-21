/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

/**
 * Dumps a tree of ValidationNode out as a text string.
 */
public class ValidationTextFormatter {
  private static final char VBAR = '\u2502';
  private static final char TOP_BAR = '\u250c';
  private static final char CHILD_TOP_BAR = '\u251c';
  private static final char BOTTOM_BAR = '\u2514';
  private static final char HBAR = '\u2500';
  
  public static String formatTree(ValidationNode root) {
    StringBuilder sb = new StringBuilder();
    formatTree(sb, root, "", true);
    return sb.toString();
  }
  
  private static void formatTree(StringBuilder sb, ValidationNode current, String prefix, boolean isTail) {
    if (current.isError()) {
      sb.append("E ");
    } else if (current.getWarnings().size() > 0) {
      sb.append("W ");
    } else {
      sb.append("  ");
    }
    sb.append(prefix);
    if (isTail) {
      sb.append(BOTTOM_BAR);
    } else {
      sb.append(CHILD_TOP_BAR);
    }
    sb.append(HBAR);
    printNode(sb, current);
    for (int i = 0; i <  current.getSubNodes().size() - 1; i++) {
      ValidationNode child = current.getSubNodes().get(i);
      formatTree(sb, child, prefix + (isTail ? "   " : (VBAR + "  ")), false);     
    }
    if (current.getSubNodes().size() >= 1) {
      ValidationNode child = current.getSubNodes().get(current.getSubNodes().size() - 1);
      formatTree(sb, child, prefix + (isTail ? "   " : (VBAR + "  ")), true);           
    }
  }
  
  private static void printNode(StringBuilder sb, ValidationNode node) {
    sb.append(" ");
    sb.append(node.getName());
    sb.append(" (");
    sb.append(node.getType().getSimpleName());
    sb.append(")");
    for (String error : node.getErrors()) {
      sb.append(" Error:");
      sb.append(error);
    }
    for (String warning : node.getWarnings()) {
      sb.append(" Warning:");
      sb.append(warning);
    }
    sb.append("\n");    
  }
}
