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
  <div class="ui-layout-center">
    <div class="new_trade" style="border-radius: 8px 8px 8px 8px;border-style: solid;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.6);color: black;cursor: pointer;padding: 10px 13px;
        margin: 5px;">NEW TRADE</div>
  </div>
</div>
${ogScript.print('og_common.js',false)}
<!--[if IE]>${ogScript.print('ie.js',false)}<![endif]-->
${ogScript.print('og_blotter.js',false)}
</body>
</html>