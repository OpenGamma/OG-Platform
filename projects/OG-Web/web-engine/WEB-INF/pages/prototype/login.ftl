<#include "modules/common/og.common.header.ftl">
<title>OpenGamma</title>
${ogStyle.print('og_all.css', 'all', false)}
</head>
<body>
<div class="OG-login">
  <img src="/prototype/images/common/logos/opengamma_shiny.png" alt="OpenGamma Logo" />
  <form>
    <table>
      <tr><td><label for="username">Username</label></td><td><input type="text" value="Guest" id="username" /></td></tr>
      <tr><td>
        <label for="password">Password</label></td><td><input type="password" value="thisisapassword" id="password" />
      </td></tr>
      <tr><td></td><td><button>Login</button></td></tr>
    </table>
  </form>
</div>
${ogScript.print('og_common.js', false)}
<!--[if lt IE 9]>${ogScript.print('ie.js', false)}<![endif]-->
${ogScript.print('og_admin.js', false)}
<!--${ogScript.print('og_analytics.js', false)}-->
<script type="text/javascript">
  $('.OG-login button').focus().on('click', function () {
      return window.location = '/jax/bundles/fm/prototype/analytics.ftl', false;
  });
</script>
</body>
</html>