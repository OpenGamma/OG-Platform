/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.extsql;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * A bundle of extsql formatted SQL.
 * <p>
 * The bundle encapsulates the SQL needed for a particular feature.
 * This will typically correspond to a data access object, or set of related tables.
 * <p>
 * This class is immutable and thread-safe.
 */
final class ExtSqlParser {

  /**
   * The regex for @NAME(identifier).
   */
  private static final Pattern NAME_PATTERN = Pattern.compile("[ ]*[@]NAME[(]([A-Za-z0-9_]+)[)][ ]*");
  /**
   * The regex for @AND(identifier).
   */
  private static final Pattern AND_PATTERN = Pattern.compile("[ ]*[@]AND[(]([:][A-Za-z0-9_]+)" + "([ ]?[=][ ]?[A-Za-z0-9_]+)?" + "[)][ ]*");
  /**
   * The regex for @OR(identifier).
   */
  private static final Pattern OR_PATTERN = Pattern.compile("[ ]*[@]OR[(]([:][A-Za-z0-9_]+)" + "([ ]?[=][ ]?[A-Za-z0-9_]+)?" + "[)][ ]*");
  /**
   * The regex for @IF(identifier).
   */
  private static final Pattern IF_PATTERN = Pattern.compile("[ ]*[@]IF[(]([:][A-Za-z0-9_]+)" + "([ ]?[=][ ]?[A-Za-z0-9_]+)?" + "[)][ ]*");
  /**
   * The regex for @INSERT(key)
   */
  private static final Pattern INSERT_PATTERN = Pattern.compile("[@]INSERT[(]([:]?[A-Za-z0-9_]+)[)](.*)");
  /**
   * The regex for text :variable text
   */
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("([^:])*([:][A-Za-z0-9_]+)(.*)");

  /**
   * The input.
   */
  private final List<Line> _lines = new ArrayList<Line>();
  /**
   * The parsed output.
   */
  private Map<String, NameSqlFragment> _namedFragments = new LinkedHashMap<String, NameSqlFragment>();

  /**
   * Creates the parser.
   * 
   * @param lines  the lines, not null
   */
  ExtSqlParser(List<String> lines) {
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      _lines.add(new Line(line, i + 1));
    }
  }

  //-------------------------------------------------------------------------
  Map<String, NameSqlFragment> parse() {
    rejectTabs();
    parseNamedSections();
    return _namedFragments;
  }

  private void rejectTabs() {
    for (Line line : _lines) {
      if (line.containsTab()) {
        throw new IllegalArgumentException("Tab character not permitted: " + line);
      }
    }
  }

  private void parseNamedSections() {
    ContainerSqlFragment containerFragment = new ContainerSqlFragment();
    parseContainerSection(containerFragment, _lines.listIterator(), -1);
  }

  private void parseContainerSection(ContainerSqlFragment container, ListIterator<Line> lineIterator, int indent) {
    while (lineIterator.hasNext()) {
      Line line = lineIterator.next();
      if (line.isComment()) {
        lineIterator.remove();
        continue;
      }
      if (line.indent() <= indent) {
        lineIterator.previous();
        return;
      }
      String trimmed = line.lineTrimmed();
      if (trimmed.startsWith("@NAME")) {
        Matcher nameMatcher = NAME_PATTERN.matcher(trimmed);
        if (nameMatcher.matches() == false) {
          throw new IllegalArgumentException("@NAME found with invalid format: " + line);
        }
        NameSqlFragment nameFragment = new NameSqlFragment(nameMatcher.group(1));
        parseContainerSection(nameFragment, lineIterator, line.indent());
        if (nameFragment.getFragments().size() == 0) {
          throw new IllegalArgumentException("@NAME found with no subsequent indented lines: " + line);
        }
        container.addFragment(nameFragment);
        _namedFragments.put(nameFragment.getName(), nameFragment);
        
      } else if (indent < 0) {
        throw new IllegalArgumentException("Invalid fragment found at root level, only @NAME is permitted: " + line);
        
      } else if (trimmed.startsWith("@WHERE")) {
        if (trimmed.equals("@WHERE") == false) {
          throw new IllegalArgumentException("@WHERE found with invalid format: " + line);
        }
        WhereSqlFragment whereFragment = new WhereSqlFragment();
        parseContainerSection(whereFragment, lineIterator, line.indent());
        if (whereFragment.getFragments().size() == 0) {
          throw new IllegalArgumentException("@WHERE found with no subsequent indented lines: " + line);
        }
        container.addFragment(whereFragment);
        
      } else if (trimmed.startsWith("@AND")) {
        Matcher andMatcher = AND_PATTERN.matcher(trimmed);
        if (andMatcher.matches() == false) {
          throw new IllegalArgumentException("@AND found with invalid format: " + line);
        }
        AndSqlFragment andFragment = new AndSqlFragment(andMatcher.group(1), StringUtils.strip(andMatcher.group(2), " ="));
        parseContainerSection(andFragment, lineIterator, line.indent());
        if (andFragment.getFragments().size() == 0) {
          throw new IllegalArgumentException("@AND found with no subsequent indented lines: " + line);
        }
        container.addFragment(andFragment);
        
      } else if (trimmed.startsWith("@OR")) {
        Matcher orMatcher = OR_PATTERN.matcher(trimmed);
        if (orMatcher.matches() == false) {
          throw new IllegalArgumentException("@OR found with invalid format: " + line);
        }
        OrSqlFragment orFragment = new OrSqlFragment(orMatcher.group(1), StringUtils.strip(orMatcher.group(2), " ="));
        parseContainerSection(orFragment, lineIterator, line.indent());
        if (orFragment.getFragments().size() == 0) {
          throw new IllegalArgumentException("@OR found with no subsequent indented lines: " + line);
        }
        container.addFragment(orFragment);
        
      } else if (trimmed.startsWith("@IF")) {
        Matcher ifMatcher = IF_PATTERN.matcher(trimmed);
        if (ifMatcher.matches() == false) {
          throw new IllegalArgumentException("@IF found with invalid format: " + line);
        }
        IfSqlFragment ifFragment = new IfSqlFragment(ifMatcher.group(1), StringUtils.strip(ifMatcher.group(2), " ="));
        parseContainerSection(ifFragment, lineIterator, line.indent());
        if (ifFragment.getFragments().size() == 0) {
          throw new IllegalArgumentException("@IF found with no subsequent indented lines: " + line);
        }
        container.addFragment(ifFragment);
        
      } else {
        parseLine(container, line);
      }
    }
  }

  private void parseLine(ContainerSqlFragment container, Line line) {
    String trimmed = line.lineTrimmed();
    if (trimmed.length() == 0) {
      return;
    }
    if (trimmed.contains("@INSERT")) {
      parseInsertTag(container, line);
      
    } else  if (trimmed.contains("@LIKE")) {
      parseLikeTag(container, line);
      
    } else  if (trimmed.contains("@OFFSETFETCH")) {
      parseOffsetFetchTag(container, line);
      
    } else if (trimmed.startsWith("@")) {
      throw new IllegalArgumentException("Unknown tag at start of line: " + line);
      
    } else {
      TextSqlFragment textFragment = new TextSqlFragment(trimmed);
      container.addFragment(textFragment);
    }
  }

  private void parseInsertTag(ContainerSqlFragment container, Line line) {
    String trimmed = line.lineTrimmed();
    int pos = trimmed.indexOf("@INSERT");
    TextSqlFragment textFragment = new TextSqlFragment(trimmed.substring(0, pos));
    Matcher insertMatcher = INSERT_PATTERN.matcher(trimmed.substring(pos));
    if (insertMatcher.matches() == false) {
      throw new IllegalArgumentException("@INSERT found with invalid format: " + line);
    }
    InsertSqlFragment insertFragment = new InsertSqlFragment(insertMatcher.group(1));
    String remainder = insertMatcher.group(2);
    container.addFragment(textFragment);
    container.addFragment(insertFragment);
    
    Line subLine = new Line(remainder, line.lineNumber());
    parseLine(container, subLine);
  }

  private void parseLikeTag(ContainerSqlFragment container, Line line) {
    String trimmed = line.lineTrimmed();
    int pos = trimmed.indexOf("@LIKE");
    TextSqlFragment beforeTextFragment = new TextSqlFragment(trimmed.substring(0, pos));
    trimmed = trimmed.substring(pos + 5);
    String content = trimmed;
    int end = trimmed.indexOf("@ENDLIKE");
    String remainder = "";
    if (end >= 0) {
      content = trimmed.substring(0, end);
      remainder = trimmed.substring(end + 8);
    }
    TextSqlFragment contentTextFragment = new TextSqlFragment(content);
    Matcher matcher = VARIABLE_PATTERN.matcher(content);
    if (matcher.matches() == false) {
      throw new IllegalArgumentException("@LIKE found with invalid format: " + line);
    }
    LikeSqlFragment likeFragment = new LikeSqlFragment(matcher.group(2));
    container.addFragment(beforeTextFragment);
    container.addFragment(likeFragment);
    likeFragment.addFragment(contentTextFragment);
    
    Line subLine = new Line(remainder, line.lineNumber());
    parseLine(container, subLine);
  }

  private void parseOffsetFetchTag(ContainerSqlFragment container, Line line) {
    String trimmed = line.lineTrimmed();
    int pos = trimmed.indexOf("@OFFSETFETCH");
    TextSqlFragment textFragment = new TextSqlFragment(trimmed.substring(0, pos));
    OffsetFetchSqlFragment pagingFragment = new OffsetFetchSqlFragment();
    String remainder = trimmed.substring(pos + 12);
    container.addFragment(textFragment);
    container.addFragment(pagingFragment);
    
    Line subLine = new Line(remainder, line.lineNumber());
    parseLine(container, subLine);
  }

  //-------------------------------------------------------------------------
  static final class Line {
    private final String _line;
    private final String _trimmed;
    private final int _lineNumber;

    Line(String line, int lineNumber) {
      _line = line;
      _trimmed = line.trim();
      _lineNumber = lineNumber;
    }

    String line() {
      return _line;
    }

    String lineTrimmed() {
      return _trimmed;
    }

    int lineNumber() {
      return _lineNumber;
    }

    boolean containsTab() {
      return _line.contains("\t");
    }

    boolean isComment() {
      return _trimmed.startsWith("--") || _trimmed.length() == 0;
    }

    int indent() {
      for (int i = 0; i < _line.length(); i++) {
        if (_line.charAt(i) != ' ') {
          return i;
        }
      }
      return _line.length();
    }

    @Override
    public String toString() {
      return "Line " + lineNumber();
    }
  }

}
