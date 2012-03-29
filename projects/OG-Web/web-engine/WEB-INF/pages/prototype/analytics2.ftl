<#include "modules/common/og.common.header.ftl">
<title>OpenGamma</title>
${ogStyle.print('og_all.css', 'all',false)}
</head>
<body>
<div class="OG-layout-analytics-container">
  <div class="ui-layout-center">
    <div class="OG-layout-analytics-center">main grid</div>
    <div class="OG-layout-analytics-south">dep graph</div>
  </div>
  <div class="ui-layout-north">header</div>
  <div class="ui-layout-south OG-analytics-footer">
    <div class="OG-logo-light-small"><small>Analytics</small></div>
  </div>
  <div class="ui-layout-east">docks</div>
</div>
<!--[if IE]>${ogScript.print('ie.js',false)}<![endif]-->
${ogScript.print('og_analytics2.js',false)}
</body>
</html>