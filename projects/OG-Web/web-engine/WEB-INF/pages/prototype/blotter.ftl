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
      <div class="van_swap">VANILLA SWAP</div>
      <div class="var_swap">VARIANCE SWAP</div>
      <div class="swaption">SWAPTION</div>
      <div class="fx_for">FX FORWARD</div>
      <div class="fx_bar_op">FX BARRIER OPTION</div>
      <div class="fx_op">FX OPTION</div>
      <div class="nd_fx_op">NON-DELIVERABLE FX OPTION</div>
  </div>
</div>
<!--[if IE]>${ogScript.print('ie.js',false)}<![endif]-->
${ogScript.print('og_common.js',false)}
${ogScript.print('og_blotter.js',false)}
</body>
</html>