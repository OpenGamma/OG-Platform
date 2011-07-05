<!doctype html>
<!--[if IE 8 ]><html lang="en" class="no-js ie8"><![endif]-->
<!--[if IE 9 ]><html lang="en" class="no-js ie9"><![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"> <!--<![endif]-->
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
<meta name="SKYPE_TOOLBAR" content="SKYPE_TOOLBAR_PARSER_COMPATIBLE" />
<title>OpenGamma</title>
<!--[if lt IE 9]><script type="text/javascript" src="/prototype/scripts/lib/html5.js"></script><![endif]-->
${ogStyle.print('new-og-all.css', 'all',false)}
</head>
<body>
<style type="text/css">


  /*
   *
   * Core
   * og.common.core.css
   *
   */
  html {
    background-color: #cdcdcd;
    background-image: -moz-radial-gradient(cover, #fafafa, #cdcdcd);
    background-image: -webkit-radial-gradient(cover, #fafafa, #cdcdcd);
    background-image: radial-gradient(cover, #fafafa, #cdcdcd);
    height: 100%;
  }

  body {
    background: none;
    width: 100%;
    height: 100%;
    position: absolute;
  }


  /*
   *
   * Container
   * og.common.OG-container.css
   *
   */
  .OG-container {
    padding: 5px;
  }





  /*
   *
   * Footer
   * og.common.OG-footer.css
   *
   */
  .OG-footer {
    bottom: 5px;
    left: 5px;
    position: absolute;
    height: 25px;
    right: 5px;
  }

  .OG-footer small {
    color: #fff;
    padding: 5px 0 0 135px;
    display: inline-block;
  }





  /*
   *
   * Masthead
   * og.common.OG-masthead.css
   *
   */
   .OG-masthead {
    top: 5px;
    left: 8px;
    position: absolute;
    right: 5px;
    height: 3em;
  }

  .OG-masthead li {
    display: inline-block;
    font-family: 'Microsoft New Tai Lue';
    margin: 0 5px 0 0;
  }

  .OG-masthead a {
    color: #555;
    display: inline-block;
    padding: 7px 5px 14px 5px;
    text-decoration: none;
  }

  .OG-masthead a:hover {
    color: #fff;
  }

  .OG-masthead .og-active {
    -webkit-border-top-left-radius: 2px;
    -webkit-border-top-right-radius: 2px;
    -moz-border-radius-topleft: 2px;
    -moz-border-radius-topright: 2px;
    background: #363f47;
    border-top-left-radius: 2px;
    border-top-right-radius: 2px;
    color: #fff;
    position: relative;
  }

  .OG-masthead .og-active:before {
    -moz-box-shadow: 0 100px 1.5em #fff;
    -webkit-box-shadow: 0 100px 1.5em #fff;
    background-color: transparent;
    content: "";
    display: inline-block;
    height: 80%;
    opacity: 0.2;
    position: absolute;
    top: -96px;
    left: 10%;
    width: 80%;
  }

  .OG-masthead a:hover.og-home:after {
    background-position: -20px -75px;
  }

  .OG-masthead .og-home.og-active:before {
    background-position: -20px -75px;
  }

  .OG-masthead .og-home {
    width: 18px;
  }

  .OG-masthead a {
    border-bottom: none;
  }

  .OG-masthead .og-home:after {
    background: url('/prototype/images/common/sprites/main-sprite-sheet.png');
    content: "";
    cursor: pointer;
    background-position: 0 -75px;
    height: 16px;
    width: 16px;
    position: absolute;
    top: 8px; left: 6px;
  }





  /*
   *
   * OG Logo Light
   * og.common.OG-logo-light.css
   *
   */
  .OG-logo-light:before {
    background: url('/prototype/images/common/sprites/main-sprite-sheet.png');
    content: "";
    cursor: pointer;
    background-position: 0 -91px;
    height: 22px;
    width: 126px;
    position: absolute;
  }



  /*
   *
   * OG Txt Shadow
   * og.common.OG-txt-shadow.css
   *
   */
  .OG-txt-shadow {
    text-shadow: 0 1px 1px #999;
  }




  /*
   *
   * Main
   * og.common.OG-main.css
   *
   */
  .OG-main {
    margin-top: 3em;
    position: relative;
    z-index: 1;
  }



  /*
   *
   * Headers
   * og.common.OG-headers.css
   *
   */
  h1 {font-size: 30px; line-height: 30px; margin-bottom: 0; font-family: 'Microsoft New Tai Lue'} /* TODO: @font-face */
  h2 {font-size: 20px; line-height: 20px; margin-bottom: 0; font-family: 'Microsoft New Tai Lue'} /* TODO: @font-face */
  h3 {font-size: 16px; font-weight: normal; margin-bottom: 2px;}




  /*
   *
   * Grid
   * og.common.OG-grid.css
   *
   */
  .OG-grid {}
  .OG-c1 {width: 10%; display: inline-block; float:left;}
  .OG-c2 {width: 20%; display: inline-block; float:left;}
  .OG-c3 {width: 30%; display: inline-block; float:left;}
  .OG-c4 {width: 40%; display: inline-block; float:left;}
  .OG-c5 {width: 50%; display: inline-block; float:left;}
  .OG-c6 {width: 60%; display: inline-block; float:left;}
  .OG-c7 {width: 70%; display: inline-block; float:left;}
  .OG-c8 {width: 80%; display: inline-block; float:left;}
  .OG-c9 {width: 90%; display: inline-block; float:left;}
  .OG-c10 {width: 100%; display: inline-block; float:left;}

  /* If OG-box is a direct descendant of an OG-cx class then give it a right margin */
  .OG-c1 > .OG-box,
  .OG-c2 > .OG-box,
  .OG-c3 > .OG-box,
  .OG-c4 > .OG-box,
  .OG-c5 > .OG-box,
  .OG-c6 > .OG-box,
  .OG-c7 > .OG-box,
  .OG-c8 > .OG-box,
  .OG-c9 > .OG-box,
  .OG-c10 > .OG-box {margin: 0 5px 5px 0;}

  /* Except for the last one */
  .OG-grid .og-grid-last > section,
  .OG-grid .og-grid-last > div,
  .OG-grid .og-grid-last > h3 {margin-right: 0;}
  .OG-grid .og-grid-last {float: none;}




  /*
   *
   * Box
   * og.common.OG-box.css
   *
   */
  .OG-box {
    -webkit-border-radius: 5px;
    -moz-border-radius: 5px;
    background: #fff;
    border: 1px solid #c9c9c9;
    border-radius: 5px;
    padding: 10px;
    position: relative;
    overflow: visible;
  }

  .OG-box.og-box-error {
    background: #dd5252;
    border: none;
    color: #fff;
    position: relative;
  }

  .OG-box.og-box-glass:after {
    -webkit-border-radius: 5px;
    -webkit-border-bottom-right-radius: 2px;
    -webkit-border-bottom-left-radius: 2px;
    -moz-border-radius: 5px;
    -moz-border-radius-bottomright: 2px;
    -moz-border-radius-bottomleft: 2px;
    background: #fff;
    border-radius: 5px;
    border-bottom-right-radius: 2px;
    border-bottom-left-radius: 2px;
    bottom: 50%;
    content: '';
    left: 1px;
    opacity: 0.2;
    position: absolute;
    top: 1px;
    right: 1px;
  }

  .OG-box.og-box-hat:after {
    -webkit-border-top-left-radius: 5px;
    -webkit-border-top-right-radius: 5px;
    -moz-border-radius-topleft: 5px;
    -moz-border-radius-topright: 5px;
    border-top-left-radius: 5px;
    border-top-right-radius: 5px;
    content: '';
    background-color: #2b343c;
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
  }





  /*
   *
   * Box Shadow Bottom
   * og.common.OG-box-shadow-bottom.css
   *
   */
  .OG-box-shadow-bottom:before {
    /* Use border radius to fake perspective */
    -webkit-border-radius: 25px;
    -webkit-border-bottom-right-radius: 5px;
    -webkit-border-bottom-left-radius: 5px;
    -moz-border-radius: 25px;
    -moz-border-radius-bottomright: 5px;
    -moz-border-radius-bottomleft: 5px;
    border-radius: 25px;
    border-bottom-right-radius: 5px;
    border-bottom-left-radius: 5px;

    /* Shadow */
    -moz-box-shadow: 0 100px 0.1em #000;
    -webkit-box-shadow: 0 100px 0.2em #000;

    background-color: transparent;
    content: "";
    display: inline-block;
    height: 10px;
    opacity: 0.15;
    position: absolute;
    bottom: 96px;
    left: -0.5%;
    width: 101%;
    z-index: -1;
  }




  /*
   *
   * History
   * og.common.OG-box-history.css
   *
   */
  .OG-box-history {
    border: 2px solid #fd0;
  }

  .OG-box-history header {
    -webkit-border-top-left-radius: 10px;
    -webkit-border-top-right-radius: 10px;
    -moz-border-radius-topleft: 10px;
    -moz-border-radius-topright: 10px;
    background-color: #fffdf2;
    border-top-left-radius: 10px;
    border-top-right-radius: 10px;
    margin: -10px -10px 0 -10px;
    padding: 10px;
  }



  /*
   *
   * Shadow light
   * og.common.OG-shadow-light.css
   *
   */
  .OG-shadow-light {
    -moz-box-shadow: 0 0 2px #bbb;
    -webkit-box-shadow: 0 0 2px #bbb;
    box-shadow: 0 0 2px #bbb;
  }




  /*
   *
   * OG Modules
   * og.common.OG-mod.css
   *
   */
  /* A generic module for displaying a header with some content, links, text etc */
  .OG-mod-1, .OG-mod-0 {margin: 30px 0 0 0;}
  .OG-mod-1 h3 {border-bottom: 4px solid #eee; margin-bottom: 1px; margin-right: 10px;}
  .OG-mod-1 > div {padding: 5px; margin-right: 10px;}
  .OG-mod-1 .og-mod-content {background: #ecf5fa;}




  /*
   *
   * OG Filter
   * og.common.OG-filter.css
   *
   */
  .OG-filter {
    position: relative;
    background: #a3ceec;
  }

  .OG-filter.og-filter-round {
    -webkit-border-radius: 2px;
    -moz-border-radius: 2px;
    border-radius: 2px;
  }

  .OG-filter.og-filter-round.og-filter-glass:after {
    -webkit-border-radius: 3px;
    -webkit-border-bottom-right-radius: 2px;
    -webkit-border-bottom-left-radius: 2px;
    -moz-border-radius: 3px;
    -moz-border-radius-bottomright: 2px;
    -moz-border-radius-bottomleft: 2px;
    border-radius: 3px;
    border-bottom-right-radius: 2px;
    border-bottom-left-radius: 2px;
  }

  .OG-filter.og-filter-glass:after {
    background: #fff;
    bottom: 50%;
    content: '';
    left: 1px;
    opacity: 0.3;
    position: absolute;
    top: 1px;
    right: 1px;
  }

  /*
   *
   * OG-header-generic
   * og.common.header.generic
   *
   */
   .OG-header-generic {
     position: relative;
   }

   .OG-header-generic small {
     font-size: 10px;
     color: #999;
   }


  /*
   *
   * OG-max-width
   * og.common.OG-max-width.css
   *
   */
   .OG-max-width-xs {max-width: 300px}
   .OG-max-width-s {max-width: 400px}
   .OG-max-width-m {max-width: 600px}
   .OG-max-width-l {max-width: 800px}
   .OG-max-width-xl {max-width: 1000px}


   /*
    *
    * Lists
    * og.common.lists.css
    *
    */
    .OG-list {
      margin: 15px 10px  15px  25px;
    }

    .OG-list li {
      list-style: none;
      position: relative;
    }

    .OG-list li:before {
      background: url('/prototype/images/common/sprites/main-sprite-sheet.png');
      content: "";
      background-position: 0 0;
      display: inline-block;
      height: 3px;
      width: 3px;
      position: absolute;
      top: 9px;
      left: -6px;
    }

   /*
    *
    * Toolbar
    * og.common.toolbar.css
    *
    */
  .OG-toolbar {
    right: 1px;
    top: 1px;
    position: absolute;
    z-index: 1;
  }

  .OG-toolbar div {
    display: inline-block;
    float: left;
    cursor: pointer;
  }

  .OG-toolbar div:after { /* glass effect */
    background: #fff;
    bottom: 50%;
    content: '';
    left: 1px;
    opacity: 0.3;
    position: absolute;
    top: 1px;
    right: 1px;
  }

  .OG-toolbar span {
    display: inline-block;
    color: #fff;
    padding-top: 1px;
    padding-bottom: 1px;
    padding-right: 5px;
    border-bottom: 1px solid #fff;
  }

  .OG-toolbar .og-delete {
    -webkit-border-bottom-left-radius: 2px;
    -moz-border-radius-bottomleft: 2px;
    border-bottom-left-radius: 3px;
    border-bottom: 1px solid #ed5d5d;
    border-left: 1px solid #ed5d5d;
    position: relative;
  }

  .OG-toolbar .og-delete span {
    background-color: #ed5d5d;
    border-left: 1px solid #fff;
    padding-left: 13px;
  }

  .OG-toolbar .og-delete span:after {
    background: url('/prototype/images/common/sprites/main-sprite-sheet.png');
    content: "";
    background-position: -14px -9px;
    height: 6px;
    width: 7px;
    position: absolute;
    top: 8px; left: 5px;
  }

  .OG-toolbar .og-new {
    border-bottom: 1px solid #64d153;
    border-left: 1px solid #fff;
    position: relative;
  }

  .OG-toolbar .og-new span {
    -webkit-border-top-right-radius: 4px;
    -moz-border-radius-topright: 4px;
    border-top-right-radius: 4px;
    background-color: #64d153;
    padding-left: 13px;
    display: inline-block;
  }

  .OG-toolbar .og-new span:after {
    background: url('/prototype/images/common/sprites/main-sprite-sheet.png');
    content: "";
    background-position: -7px -9px;
    height: 6px;
    width: 6px;
    position: absolute;
    top: 8px; left: 5px;
  }


  /*
   *
   * Disabled
   * og.common.classes
   *
   */
  .OG-disabled {
    opacity: .3;
  }


</style>

<div class="OG-container">

<section class="OG-masthead">
  <ul>
    <li><a href="" class="og-home">&nbsp;</a></li>
    <li><a href="">analytics</a></li>
    <li><a href="" class="og-active">analytics</a></li>
    <li><a href="">portfolios</a></li>
    <li><a href="">positions</a></li>
    <li><a href="">securities</a></li>
    <li><a href="">timeseries</a></li>
    <li><a href="">exchanges</a></li>
    <li><a href="">holidays</a></li>
    <li><a href="">batches</a></li>
    <li><a href="">configs</a></li>
  </ui>
</section>
<section class="OG-main">
  <div class="OG-grid">
    <div class="OG-c3">
      <section class="OG-box og-box-hat">
        <div class="OG-filter og-filter-glass" style="margin: -6px -10px -10px -10px; padding: 5px 5px 7px 5px">
          <input type="text">
        </div>
        <br />
        search results<br />search results<br />search results<br />search results<br />search results<br />search results<br />
        search results<br />search results<br />search results<br />search results<br />search results<br />search results<br />
        search results<br />search results<br />search results<br />search results<br />search results<br />search results<br />
      </section>
    </div>
    <div class="OG-c7 og-grid-last">
      <section class="OG-box og-box-glass og-box-error OG-shadow-light">This position has been deleted</section>
      <section class="OG-box OG-details">
        <div class="OG-toolbar">
          <div class="og-delete"><span>delete</span></div>
          <div class="og-new"><span>new</span></div>
        </div>
        <header class="OG-header-generic">
          <h1>4892 x GD US Equity</h1>
          <small>version 9468 / <a href="">view latest (10534)</a>, <a href="">previous</a>, <a href="">next</a></small>
        </header>
        <div class="OG-c10 og-grid-last">
          <section class="OG-mod-0 og-grid-last OG-max-width-l">
            <h2>What's new?</h2>
            <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc sed suscipit purus. Curabitur lacinia suscipit dolor nec condimentum. Nam ligula nulla, imperdiet a pharetra ac, accumsan at nibh. Vivamus et rutrum diam.</p>
            <ul class="OG-list">
              <li><a href="">Long bond view</a> (analytics)</li>
              <li><a href="">Test Bond Future Portfolio</a> (portfolio)</li>
              <li><a href="">Test Single Bond Portfolio</a> (portfolio)</li>
              <li><a href="">Test Equity Portfolio 12</a> (portfolio)</li>
              <li><a href="">Test Equity Option Portfolio aggregated by Detailed Asset Class</a> (portfolio)</li>
              <li><a href="">Test Swap View</a> (portfolio)</li>
              <li><a href="">90DAY EURO$ FUTR Dec02</a> (security)</li>
            </ul>
          </section>
        </div>
        <div class="OG-c10 og-grid-last OG-max-width-l">
          <section class="OG-mod-1 OG-c5">
            <h3>Portfolios</h3>
            <div class="OG-filter og-filter-glass og-filter-round">
              <input type="text" />
            </div>
            <div class="og-mod-content">some content<br />some content<br />some content</div>
            <a href="#" class="OG-link-add">add new portfolio</a>
          </section>
          <section class="OG-mod-1 OG-c5 og-grid-last">
            <h3>Positions</h3>
            <div class="OG-filter og-filter-glass og-filter-round">
              <input type="text" />
            </div>
            <div class="og-mod-content">some content<br />some content<br />some content</div>
            <a href="#" class="OG-link-add">add new position</a>
          </section>
          <section class="OG-mod-1 OG-c10 og-grid-last">
            <h3>Security</h3>
            <div class="og-mod-content">some content<br />some content<br />some content</div>
          </section>
          <section class="OG-tags">
            Tags:
          </section>
        </div>
      </section>
      <section class="OG-box OG-box-history OG-box-shadow-bottom" style="position: relative">
        <header>
          <h2>Version History</h2>
        </header>
        content
      </section>
    </div>
  </div>
</section>
<section class="OG-footer">
  <div class="OG-logo-light"><small class="OG-txt-shadow">&copy; 2011 OpenGamma Limited</small></div>
</section>

<div style="display: none">
  <#include "modules/common/og.common.masthead.ftl">
  <#include "modules/common/og.common.search_results.ftl">
  <#include "modules/common/og.common.details.ftl">
  <#include "modules/common/og.common.analytics.ftl">
</div>

<!--[if IE]>${ogScript.print('ie.js',false)}<![endif]-->
${ogScript.print('og_all.js',false)}

</body>
</html>