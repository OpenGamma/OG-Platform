/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.details.favorites',
    dependencies: [],
    obj: function () {
        return function () {
            $('.OG-icon.og-favorites').click(function () {$(this).toggleClass('og-favorites-active');});
        };
    }
});


