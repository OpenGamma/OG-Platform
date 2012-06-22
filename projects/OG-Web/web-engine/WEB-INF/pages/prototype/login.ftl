<#include "modules/common/og.common.header.ftl">
<title>OpenGamma</title>
${ogStyle.print('og_all.css', 'all', false)}
</head>
<body>
<div class="OG-login">
  <img src="/prototype/images/common/logos/opengamma_shiny.png" alt="OpenGamma Logo" />
  <form>
    <table>
      <tr><td><label for="username">Username</label></td><td><input type="text" id="username" /></td></tr>
      <tr><td><label for="password">Password</label></td><td><input type="text" id="password" /></td></tr>
      <tr><td></td><td><button>Login</button></td></tr>
    </table>
  </form>
</div>
<#include "modules/common/og.common.footer.ftl">
${ogScript.print('og_common.js', false)}
<!--[if lt IE 9]>${ogScript.print('ie.js', false)}<![endif]-->
${ogScript.print('og_main.js', false)}
<!--${ogScript.print('og_analytics2.js', false)}-->
</body>
</html>