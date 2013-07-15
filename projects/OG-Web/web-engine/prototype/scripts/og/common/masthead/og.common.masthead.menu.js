/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.masthead.menu',
    dependencies: [],
    obj: function () {
        return {
            init: function () {
                var link = '.OG-masthead .og-configs', menu = '.OG-masthead .og-menu', active = 'og-active-menu',
                    hovering = false, common = og.views.common;
                var hide_menu = function () {
                    $(menu).hide();
                    $(link).removeClass(active);
                    common.layout.main.resetOverflow('north');
                };
                $(link + ', ' + menu).hover(
                    function () {
                        common.layout.main.allowOverflow('north');
                        $(menu).show();
                        $(link).addClass(active);
                        hovering = true;
                    },
                    function () {
                        hovering = false;
                        setTimeout(function () {
                            if (hovering === false) hide_menu();
                        }, 500);
                    }
                ).on('click', 'a', function () {hide_menu();});
                $(link).on('click', function () {hide_menu();});
            },
            set_tab: function (name) {
                $('.OG-masthead a').removeClass('og-active');
                $('.OG-masthead .og-' + name).addClass('og-active');
            }
        };
    }
});