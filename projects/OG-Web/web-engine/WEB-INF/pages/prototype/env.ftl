<#include "modules/common/og.common.header.ftl">
<title>OpenGamma</title>
${ogStyle.print('og_all.css', 'all',false)}
${ogStyle.print('env.css', 'all',false)}
</head>
<body>
  <div class="OG-env">
    <div class="og-menu-container"></div>
    <div class="og-module-container">
      <div class="og-border">
        <div class="og-module"></div>
      </div>
    </div>
  </div>
  <!--[if IE]>${ogScript.print('ie.js',false)}<![endif]-->
  ${ogScript.print('og_common.js',false)}
  ${ogScript.print('env.js',false)}
</body>
</html>