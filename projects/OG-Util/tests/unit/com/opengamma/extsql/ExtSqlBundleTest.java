/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.extsql;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.testng.annotations.Test;

/**
 * Test.
 */
@Test
public class ExtSqlBundleTest {

  public void test_name_1name_1line() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource());
    assertEquals("SELECT * FROM foo ", sql1);
  }

  public void test_name_2names_1line() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  ",
        "@NAME(Test2)",
        "  SELECT * FROM bar"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource());
    assertEquals("SELECT * FROM foo ", sql1);
    String sql2 = bundle.getSql("Test2", new MapSqlParameterSource());
    assertEquals("SELECT * FROM bar ", sql2);
  }

  public void test_name_2names_2lines() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  WHERE TRUE",
        "  ",
        "@NAME(Test2)",
        "  SELECT * FROM bar",
        "  WHERE FALSE"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource());
    assertEquals("SELECT * FROM foo WHERE TRUE ", sql1);
    String sql2 = bundle.getSql("Test2", new MapSqlParameterSource());
    assertEquals("SELECT * FROM bar WHERE FALSE ", sql2);
  }

  public void test_name_midComments() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM",
        "--  foo",
        "  WHERE TRUE",
        "  "
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource());
    assertEquals("SELECT * FROM WHERE TRUE ", sql1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_name_invalidFormat1() {
    List<String> lines = Arrays.asList(
        "@NAME("
    );
    ExtSqlBundle.parse(lines);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_invalidFormat2() {
    List<String> lines = Arrays.asList(
        "@NAME()"
    );
    ExtSqlBundle.parse(lines);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_name_invalidFormat3() {
    List<String> lines = Arrays.asList(
        "@NAME(!)"
    );
    ExtSqlBundle.parse(lines);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_name_notFound() {
    ExtSqlBundle bundle = ExtSqlBundle.parse(new ArrayList<String>());
    bundle.getSql("Unknown", new MapSqlParameterSource());
  }

  //-------------------------------------------------------------------------
  public void test_insert_name() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM @INSERT(Table) WHERE TRUE",
        "  ",
        "@NAME(Table)",
        "  foo"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource());
    assertEquals("SELECT * FROM foo WHERE TRUE ", sql1);
    String sql2 = bundle.getSql("Table", new MapSqlParameterSource());
    assertEquals("foo ", sql2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_insert_name_notFound() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM @INSERT(Table) WHERE TRUE",
        "  "
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    bundle.getSql("Test1", new MapSqlParameterSource());
  }

  //-------------------------------------------------------------------------
  public void test_insert_variable() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM @INSERT(:var) WHERE TRUE"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("var", "foo"));
    assertEquals("SELECT * FROM foo WHERE TRUE ", sql1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_insert_variable_notFound() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM @INSERT(:var) WHERE TRUE",
        "  "
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    bundle.getSql("Test1", new MapSqlParameterSource());
  }

  //-------------------------------------------------------------------------
  public void test_like_equals() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  WHERE var @LIKE :var"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("var", "val"));
    assertEquals("SELECT * FROM foo WHERE var = :var ", sql1);
  }

  public void test_like_likePercent() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  WHERE var @LIKE :var"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("var", "va%"));
    assertEquals("SELECT * FROM foo WHERE var LIKE :var ", sql1);
  }

  public void test_like_likeUnderscore() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  WHERE var @LIKE :var"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("var", "va_"));
    assertEquals("SELECT * FROM foo WHERE var LIKE :var ", sql1);
  }

  public void test_likeEndLike_equals() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  WHERE (var @LIKE :var @ENDLIKE)"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("var", "val"));
    assertEquals("SELECT * FROM foo WHERE (var = :var ) ", sql1);
  }

  public void test_likeEndLike_like() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  WHERE (var @LIKE :var @ENDLIKE)"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("var", "va%l"));
    assertEquals("SELECT * FROM foo WHERE (var LIKE :var ) ", sql1);
  }

  public void test_likeEndLike_like_configEscape() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  WHERE (var @LIKE :var @ENDLIKE)"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    bundle = bundle.withConfig(ExtSqlConfig.HSQL);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("var", "va%l"));
    assertEquals("SELECT * FROM foo WHERE (var LIKE :var ESCAPE '\\' ) ", sql1);
  }

  //-------------------------------------------------------------------------
  public void test_offsetFetch() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @OFFSETFETCH"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    MapSqlParameterSource paramSource = new MapSqlParameterSource("paging_offset", 7).addValue("paging_fetch", 3);
    String sql1 = bundle.getSql("Test1", paramSource);
    assertEquals("SELECT * FROM foo OFFSET 7 ROWS FETCH NEXT 3 ROWS ONLY ", sql1);
  }

  //-------------------------------------------------------------------------
  public void test_if_varAbsent() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @IF(:var)",
        "    WHERE var = :var"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource());
    assertEquals("SELECT * FROM foo ", sql1);
  }

  public void test_if_varPresent() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @IF(:var)",
        "    WHERE var = :var"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("var", "val"));
    assertEquals("SELECT * FROM foo WHERE var = :var ", sql1);
  }

  public void test_if_varPresentBooleanFalse() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @IF(:var)",
        "    WHERE var = :var"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("var", Boolean.FALSE));
    assertEquals("SELECT * FROM foo ", sql1);
  }

  public void test_if_varPresentBooleanTrue() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @IF(:var)",
        "    WHERE var = :var"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("var", Boolean.TRUE));
    assertEquals("SELECT * FROM foo WHERE var = :var ", sql1);
  }

  public void test_if_varPresentBooleanFalseEqualsFalse() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @IF(:var = false)",
        "    WHERE var = :var"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("var", Boolean.FALSE));
    assertEquals("SELECT * FROM foo WHERE var = :var ", sql1);
  }

  public void test_if_varPresentBooleanFalseEqualsTrue() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @IF(:var = true)",
        "    WHERE var = :var"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("var", Boolean.FALSE));
    assertEquals("SELECT * FROM foo ", sql1);
  }

  public void test_if_varPresentBooleanTrueEqualsFalse() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @IF(:var = false)",
        "    WHERE var = :var"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("var", Boolean.TRUE));
    assertEquals("SELECT * FROM foo ", sql1);
  }

  public void test_if_varPresentBooleanTrueEqualsTrue() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @IF(:var = true)",
        "    WHERE var = :var"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("var", Boolean.TRUE));
    assertEquals("SELECT * FROM foo WHERE var = :var ", sql1);
  }

  //-------------------------------------------------------------------------
  public void test_and_1and_varAbsent() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @WHERE",
        "    @AND(:var)",
        "      var = :var"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource());
    assertEquals("SELECT * FROM foo ", sql1);
  }

  public void test_and_1and_varPresent() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @WHERE",
        "    @AND(:var)",
        "      var = :var"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("var", "val"));
    assertEquals("SELECT * FROM foo WHERE var = :var ", sql1);
  }

  public void test_and_2and_varPresentAbsent() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @WHERE",
        "    @AND(:var)",
        "      var = :var",
        "    @AND(:vax)",
        "      vax = :vax"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("var", "val"));
    assertEquals("SELECT * FROM foo WHERE var = :var ", sql1);
  }

  public void test_and_2and_varAbsentPresent() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @WHERE",
        "    @AND(:var)",
        "      var = :var",
        "    @AND(:vax)",
        "      vax = :vax"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("vax", "val"));
    assertEquals("SELECT * FROM foo WHERE vax = :vax ", sql1);
  }

  public void test_and_2and_varPresent() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @WHERE",
        "    @AND(:var)",
        "      var = :var",
        "    @AND(:vax)",
        "      vax = :vax"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    MapSqlParameterSource source = new MapSqlParameterSource("var", "val").addValue("vax", "val");
    String sql1 = bundle.getSql("Test1", source);
    assertEquals("SELECT * FROM foo WHERE var = :var AND vax = :vax ", sql1);
  }

  public void test_and_withMatch_varAbsent() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @WHERE",
        "    @AND(:var = Point)",
        "      var = :var"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource());
    assertEquals("SELECT * FROM foo ", sql1);
  }

  public void test_and_withMatch_varPresentMatch() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @WHERE",
        "    @AND(:var = Point)",
        "      var = :var"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("var", "Point"));
    assertEquals("SELECT * FROM foo WHERE var = :var ", sql1);
  }

  public void test_and_withMatch_varPresentNoMatch() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @WHERE",
        "    @AND(:var = Point)",
        "      var = :var"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("var", "NoPoint"));
    assertEquals("SELECT * FROM foo ", sql1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_and_invalidFormat1() {
    List<String> lines = Arrays.asList(
        "@AND("
    );
    ExtSqlBundle.parse(lines);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_and_invalidFormat2() {
    List<String> lines = Arrays.asList(
        "@AND()"
    );
    ExtSqlBundle.parse(lines);
  }


  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_and_invalidFormat3() {
    List<String> lines = Arrays.asList(
        "@AND(!)"
    );
    ExtSqlBundle.parse(lines);
  }

  //-------------------------------------------------------------------------
  public void test_or_1or_varAbsent() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @WHERE",
        "    @OR(:var)",
        "      var = :var"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource());
    assertEquals("SELECT * FROM foo ", sql1);
  }

  public void test_or_1or_varPresent() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @WHERE",
        "    @OR(:var)",
        "      var = :var"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("var", "val"));
    assertEquals("SELECT * FROM foo WHERE var = :var ", sql1);
  }

  public void test_or_2or_varPresentAbsent() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @WHERE",
        "    @OR(:var)",
        "      var = :var",
        "    @OR(:vax)",
        "      vax = :vax"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("var", "val"));
    assertEquals("SELECT * FROM foo WHERE var = :var ", sql1);
  }

  public void test_or_2or_varAbsentPresent() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @WHERE",
        "    @OR(:var)",
        "      var = :var",
        "    @OR(:vax)",
        "      vax = :vax"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    String sql1 = bundle.getSql("Test1", new MapSqlParameterSource("vax", "val"));
    assertEquals("SELECT * FROM foo WHERE vax = :vax ", sql1);
  }

  public void test_or_2or_varPresent() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @WHERE",
        "    @OR(:var)",
        "      var = :var",
        "    @OR(:vax)",
        "      vax = :vax"
    );
    ExtSqlBundle bundle = ExtSqlBundle.parse(lines);
    MapSqlParameterSource source = new MapSqlParameterSource("var", "val").addValue("vax", "val");
    String sql1 = bundle.getSql("Test1", source);
    assertEquals("SELECT * FROM foo WHERE var = :var OR vax = :vax ", sql1);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_topLevelNotName1() {
    List<String> lines = Arrays.asList(
        "@AND(:var)",
        "  var = :var"
    );
    ExtSqlBundle.parse(lines);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_topLevelNotName2() {
    List<String> lines = Arrays.asList(
        "SELECT foo FROM bar"
    );
    ExtSqlBundle.parse(lines);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_unknownTagAtStartLine() {
    List<String> lines = Arrays.asList(
        "@NAME(Test1)",
        "  SELECT * FROM foo",
        "  @UNKNOWN"
    );
    ExtSqlBundle.parse(lines);
  }

}
