<#include "modules/common/og.common.header.ftl">
<title>OpenGamma</title>
${ogStyle.print('og_all.css', 'all',false)}
</head>
<body>
<div class="OG-layout-blotter-container">
  <div class="ui-layout-north">
      <#include "modules/common/og.common.masthead.ftl">
      <div class="og-form"></div>
  </div>
  <div class="ui-layout-center" style="float:left">
      <div class="new_trade">NEW TRADE</div>
  </div>
</div>
<!--[if IE]>${ogScript.print('ie.js',false)}<![endif]-->
${ogScript.print('og_common.js',false)}
${ogScript.print('og_blotter.js',false)}
</body>
</html>