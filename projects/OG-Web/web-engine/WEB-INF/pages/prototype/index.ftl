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
${ogStyle.print('og_all.css', 'all',false)}
</head>
<body>
<div class="OG-container" style="display: none">
  <#include "modules/common/og.common.masthead.ftl">
  <section class="OG-main">
      <div class="OG-grid">
      <div class="OG-c3 OG-js-search-panel">
        <#include "modules/common/og.common.search_results.ftl">
        <div class="OG-resizeBar"></div>
      </div>
      <div class="OG-c7 og-grid-last OG-js-details-panel">
        <#include "modules/common/og.common.details.ftl">
      </div>
      <div class="OG-c10 OG-js-analytics-panel">
        <#include "modules/common/og.common.analytics.ftl">
      </div>
    </div>
  </section>
  <section class="OG-footer">
    <div class="OG-logo-light"><small class="OG-txt-shadow">&copy; 2011 OpenGamma Limited</small></div>
  </section>
</div>
<!--[if IE]>${ogScript.print('ie.js',false)}<![endif]-->
${ogScript.print('og_all.js',false)}
</body>
</html>