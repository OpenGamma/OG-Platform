<#-- Stylish version of login -->
<#escape x as x?html>
<html>
  <head>
    <title>Login</title>
    <link rel="stylesheet" type="text/css" href="/prototype/styles/login/og.login.OG-login.css">
  </head>
  <body onload="document.getElementById('username').focus()">
    <div class="OG-login">
      <img class="og-logo" src="/prototype/images/common/logos/opengamma_shiny.png" alt="OpenGamma Logo" />
      <form id="og-login" method="POST" action="${security.loginUri}/og">
        <table>
<#if err_invalidLogin??>
          <tr><td></td><td class="err">
              <#if err_invalidLogin = 'UserNameMissing'>The user name must be entered
              <#elseif err_invalidLogin = 'PasswordMissing'>The password must be entered
              <#elseif err_invalidLogin = 'UnknownAccount'>The user name does not exist
              <#elseif err_invalidLogin = 'IncorrectCredentials'>The password is incorrect
              <#elseif err_invalidLogin = 'ExpiredCredentials'>The password has expired
              <#elseif err_invalidLogin = 'DisabledAccount'>The account has been disabled
              <#else><div class="err">Unable to login
            </#if>
          </td><td>
</#if>
          <tr><td>
            <label for="username">Username</label>
          </td><td>
            <input type="text" maxlength="20" name="username" value="${username}" id="username" autofocus />
          </td></tr>
          <tr><td>
            <label for="password">Password</label>
          </td><td>
            <input type="password" maxlength="100" name="password" value="" id="password" />
          </td></tr>
          <tr><td></td><td>
            <button type="submit" value="Login">Login</button>
          </td></tr>
        </table>
      </form>
    </div>
  </body>
</html>
</#escape>
