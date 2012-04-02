<#include "modules/common/og.common.header.ftl">
<title>OpenGamma</title>
${ogStyle.print('og_all.css', 'all',false)}
<style type="text/css" media="screen">
  body {background: white;}
</style>
</head>
<body>
  <div id="gadget_content"></div>
  <!--[if IE]>${ogScript.print('ie.js',false)}<![endif]-->
  ${ogScript.print('og_gadget.js',false)}
</body>
</html>