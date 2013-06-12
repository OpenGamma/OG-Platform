<#include "modules/common/og.common.header.ftl">
<title>OpenGamma</title>
${ogStyle.print('og_all.css', 'all',false)}
</head>
<body>
<div class="OG-layout-analytics-container">
  <div class="ui-layout-north">
      <#include "modules/common/og.common.masthead.ftl">
  </div>
  <div class="ui-layout-center">
    <div class="OG-layout-analytics-center"></div>
    <div class="OG-layout-analytics-south">
      <#include "modules/common/og.common.gadget_container.ftl">
    </div>
  </div>
  <div class="ui-layout-east">
      <div class="OG-layout-analytics-dock-north"><#include "modules/common/og.common.gadget_container.ftl"></div>
      <div class="OG-layout-analytics-dock-center"><#include "modules/common/og.common.gadget_container.ftl"></div>
      <div class="OG-layout-analytics-dock-south"><#include "modules/common/og.common.gadget_container.ftl"></div>
  </div>
</div>
<!--[if IE]>${ogScript.print('ie.js',false)}<![endif]-->
${ogScript.print('og_common.js', false)}
${ogScript.print('og_analytics.js',false)}
</body>
</html>