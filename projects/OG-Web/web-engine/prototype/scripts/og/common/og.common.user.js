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
                $('.OG-masthead .og-logout').on('click', function () {
                    api.logout.get().pipe(function () {
                        window.location = '/jax/login/og';
                    });
                });
            }
        };
    }
});