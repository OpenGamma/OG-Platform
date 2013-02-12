<#include "modules/common/og.common.header.ftl">
<title>OpenGamma</title>
${ogStyle.print('og_all.css', 'all',false)}
<style type="text/css">
.temp-ul li{padding-left:6px;}
.temp-ul {padding:3px;}
</style>
</head>
<body>
<div class="OG-layout-blotter-container">
  <div class="ui-layout-north">
      <#include "modules/common/og.common.masthead.ftl">
      <div class="og-form"></div>
  </div>
  <div class="ui-layout-center">
    
  </div>
</div>
<!--[if IE]>${ogScript.print('ie.js',false)}<![endif]-->
${ogScript.print('og_common.js',false)}
${ogScript.print('og_blotter.js',false)}
</body>
</html>