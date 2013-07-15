<#include "modules/common/og.common.header.ftl">
<title>OpenGamma</title>
${ogStyle.print('og_all.css', 'all',false)}
</head>
<body>
<div class="OG-js-loading" style="padding: 10px">Loading...</div>
<div class="OG-layout-admin-container">
  <div class="ui-layout-center">
    <div class="OG-layout-admin-details-center">
      <div class="ui-layout-header">&nbsp;</div>
      <div class="ui-layout-content">&nbsp;</div>
    </div>
    <div class="OG-layout-admin-details-north">&nbsp;</div>
    <div class="OG-layout-admin-details-south"></div>
  </div>
  <div class="ui-layout-north"><#include "modules/common/og.common.masthead.ftl"></div>
  <div class="ui-layout-west"><#include "modules/common/og.common.search_results.ftl"></div>
</div>
<!--[if lt IE 9]>${ogScript.print('ie.js',false)}<![endif]-->
${ogScript.print('og_common.js',false)}
${ogScript.print('og_admin.js',false)}
</body>
</html>