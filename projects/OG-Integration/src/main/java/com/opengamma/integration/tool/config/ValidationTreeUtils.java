/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.util.ArrayList;

/**
 * Utility methods for processing trees of ValidationNodes
 */
public class ValidationTreeUtils {
  public static boolean containsErrors(ValidationNode validationNode) {
    if (validationNode.isError()) {
      return true;
    }
    for (ValidationNode child : validationNode.getSubNodes()) {
      if (containsErrors(child)) {
        return true;
      }
    }
    return false;
  }
  
  public static boolean containsWarnings(ValidationNode validationNode) {
    if (validationNode.getWarnings().size() > 0) {
      return true;
    }
    for (ValidationNode child : validationNode.getSubNodes()) {
      if (containsWarnings(child)) {
        return true;
      }
    }
    return false;
  }
  
  public static boolean containsErrorsOrWarnings(ValidationNode validationNode) {
    if (validationNode.isError() || validationNode.getWarnings().size() > 0) {
      return true;
    }
    for (ValidationNode child : validationNode.getSubNodes()) {
      if (containsErrorsOrWarnings(child)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Modify a tree so all nodes in a path containing an error have their error flag set.  Used to display only paths with errors present.
   * @param validationNode the root of the tree, not null
   * @return true, if the tree contains any errors
   */
  public static boolean propagateErrorsUp(ValidationNode validationNode) {
    boolean containsErrors = false;
    for (ValidationNode child : validationNode.getSubNodes()) {
      containsErrors |= propagateErrorsUp(child);
    }
    validationNode.setError(containsErrors | validationNode.isError());
    return validationNode.isError();
  }
  
  /**
   * Modify a tree so all nodes in a path containing an error or warning have their error flag set.  Used to display only paths with errors present.
   * @param validationNode the root of the tree, not null
   * @return true, if the tree contains any errors or warnings
   */
  public static boolean propagateErrorsAndWarningsUp(ValidationNode validationNode) {
    boolean containsErrors = false;
    for (ValidationNode child : validationNode.getSubNodes()) {
      containsErrors |= propagateErrorsUp(child);
    }
    validationNode.setError(containsErrors | validationNode.isError() | validationNode.getWarnings().size() > 0);
    return validationNode.isError();
  }
  
  /**
   * Modify a tree to remove all nodes not having an error flag set.  Used to display only paths with errors present.
   * @param validationNode the root of the tree, not null
   * @return tree root, if the tree contains any errors or warnings, null otherwise
   */
  public static ValidationNode discardNonErrors(ValidationNode validationNode) {
    if (!validationNode.isError()) {
      return null;
    }
    for (ValidationNode child : new ArrayList<>(validationNode.getSubNodes())) {
      if (child.isError()) {
        discardNonErrors(child);
      } else {
        validationNode.getSubNodes().remove(child);
      }
    }
    return validationNode;
  }
}
