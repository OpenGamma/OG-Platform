<#include "modules/common/og.common.header.ftl">
<title>OpenGamma</title>
${ogStyle.print('og_all.css', 'all',false)}
</head>
<body>
<div class="OG-layout-analytics-container">
  <div class="ui-layout-north"></div>
  <div class="ui-layout-center">
    <div class="OG-layout-analytics-center">main grid</div>
    <div class="OG-layout-analytics-south">
      <#include "modules/common/og.common.gadget_container.ftl">
    </div>
  </div>
  <div class="ui-layout-south OG-analytics-footer">
    <div class="OG-logo-light-small"><small>Analytics</small></div>
  </div>
  <div class="ui-layout-east">
      <div class="OG-layout-analytics-dock-north"><#include "modules/common/og.common.gadget_container.ftl"></div>
      <div class="OG-layout-analytics-dock-center"><#include "modules/common/og.common.gadget_container.ftl"></div>
      <div class="OG-layout-analytics-dock-south"><#include "modules/common/og.common.gadget_container.ftl"></div>
  </div>
</div>
<!--[if IE]>${ogScript.print('ie.js',false)}<![endif]-->
${ogScript.print('og_analytics2.js',false)}
</body>
</html>