/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.user',
    dependencies: ['og.api.common', 'og.api.rest.user'],
    obj: function () {
        var api = og.api.rest.user;
        return {
            init : function () {
                //console.log('init users');
                $('.OG-masthead .og-logout').on('click', function () {
                    api.logout.put().pipe(function () {
                        window.location = '/jax/bundles/fm/prototype/login.ftl';
                    });
                });
            }
        };
    }
});