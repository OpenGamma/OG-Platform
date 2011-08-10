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

<div class="ui-layout-container">
  <div class="ui-layout-center">
    <div class="ui-layout-inner-center">
      <div class="ui-layout-header">header</div>
      <div class="ui-layout-content">
        ui-layout-details-center
      </div>
    </div>
    <div class="ui-layout-inner-north">ui-layout-details-center-north</div>
    <div class="ui-layout-inner-south">ui-layout-details-south</div>
  </div>
  <div class="ui-layout-north">
    <#include "modules/common/og.common.masthead.ftl">
  </div>
  <div class="ui-layout-south">
    <section class="OG-footer">
      <div class="OG-logo-light"><small class="OG-txt-shadow">&copy; 2011 OpenGamma Limited</small></div>
    </section>
  </div>
  <div class="ui-layout-east">East</div>
  <div class="ui-layout-west">
      <#include "modules/common/og.common.search_results.ftl">
  </div>
</div>




<!--[if IE]>${ogScript.print('ie.js',false)}<![endif]-->
${ogScript.print('og_all.js',false)}

</body>
</html>