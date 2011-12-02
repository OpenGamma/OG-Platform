<!doctype html>
<!--[if IE 8 ]><html lang="en" class="no-js ie8"><![endif]-->
<!--[if IE 9 ]><html lang="en" class="no-js ie9"><![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"> <!--<![endif]-->
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
<meta name="SKYPE_TOOLBAR" content="SKYPE_TOOLBAR_PARSER_COMPATIBLE" />
<meta name="google" value="notranslate">
<title>OpenGamma</title>
<!--[if lt IE 9]><script type="text/javascript" src="/prototype/scripts/lib/html5.js"></script><![endif]-->
${ogStyle.print('og_all.css', 'all',false)}
</head>
<body>
<div class="OG-js-loading" style="padding: 10px">Loading...</div>
<div class="ui-layout-container" style="display: none">
  <div class="ui-layout-center">
    <div class="ui-layout-inner-center">
      <div class="ui-layout-header">&nbsp;</div>
      <div class="ui-layout-content">&nbsp;</div>
    </div>
    <div class="ui-layout-inner-north">&nbsp;</div>
    <div class="ui-layout-inner-south"></div>
  </div>
  <div class="ui-layout-north">
    <#include "modules/common/og.common.masthead.ftl">
  </div>
  <div class="ui-layout-south">
    <#include "modules/common/og.common.footer.ftl">
  </div>
  <div class="ui-layout-east"></div>
  <div class="ui-layout-west">
    <#include "modules/common/og.common.search_results.ftl">
  </div>
</div>
${ogScript.print('og_common.js',false)}
<!--[if lt IE 9]>${ogScript.print('ie.js',false)}<![endif]-->
${ogScript.print('og_main.js',false)}
</body>
</html>