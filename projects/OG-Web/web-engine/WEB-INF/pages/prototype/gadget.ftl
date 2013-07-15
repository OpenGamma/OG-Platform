<#include "modules/common/og.common.header.ftl">
<title>OpenGamma</title>
${ogStyle.print('og_all.css', 'all',false)}
<style type="text/css" media="screen">
  body {background: white; height: 100%;}
</style>
</head>
<body>
  <div class="OG-layout-gadget-container" style="height: 100%">
    <div class="ui-layout-center">
      <#include "modules/common/og.common.gadget_container.ftl">
    </div>
  </div>
  <!--[if IE]>${ogScript.print('ie.js',false)}<![endif]-->
  ${ogScript.print('og_common.js',false)}
  ${ogScript.print('og_gadget.js',false)}
</body>
</html>