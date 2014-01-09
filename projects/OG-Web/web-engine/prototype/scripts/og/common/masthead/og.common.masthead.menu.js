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
                var config_link = '.OG-masthead .og-configs', config = '.OG-masthead .og-config',
                    data = '.OG-masthead .og-data', data_link = '.OG-masthead .og-datas',
                    active = 'og-active-menu', hovering = false, common = og.views.common, menu_html;
                var hide_config = function () {
                    $(config).hide();
                    $(config_link).removeClass(active);
                    common.layout.main.resetOverflow('north');
                };
                var hide_data = function () {
                    $(data).hide();
                    $(data_link).removeClass(active);
                    common.layout.main.resetOverflow('north');
                };
                var hide_both = function () {
                    hide_data();
                    hide_config();
                };
                $(config_link + ', ' + config)
                    .hover(function () {
                        hide_data();
                        common.layout.main.allowOverflow('north');
                        $(config).show();
                        $(config_link).addClass(active);
                        hovering = true;
                    },
                        function () {
                            hovering = false;
                            setTimeout(function () {
                                if (hovering === false) {
                                    hide_config();
                                }
                            }, 500);
                        })
                    .on('click', 'a', function () {hide_config(); });
                $(data_link + ', ' + data)
                    .hover(function () {
                        hide_config();
                        common.layout.main.allowOverflow('north');
                        $(data).show();
                        $(data_link).addClass(active);
                        hovering = true;
                    },
                        function () {
                            hovering = false;
                            setTimeout(function () {
                                if (hovering === false) {
                                    hide_data();
                                }
                            }, 500);
                        })
                    .on('click', 'a', function () {hide_data(); });

                $(config_link).on('click', function () {hide_both(); });
                $(data_link).on('click', function () {hide_both(); });
                og.api.rest.configs.get({meta: true}).pipe(function (result) {
                    menu_html = '<table><tr><td><ul>';
                    var types = result.data.types.sort(),
                        left = types.splice(0, Math.ceil(types.length / 2));
                    left.forEach(function (entry) {
                        menu_html += '<li><a href="admin.ftl#/configs/filter=true/type=' + entry.value + '">' + entry.name + '</a></li>';
                    });
                    menu_html += '</ul></td><td><ul>';
                    types.forEach(function (entry) {
                        menu_html += '<li><a href="admin.ftl#/configs/filter=true/type=' + entry.value + '">' + entry.name + '</a></li>';
                    });
                    menu_html += '</ul></td></table>';
                    $(config).html(menu_html);
                });
            },
            set_tab: function (name) {
                $('.OG-masthead a').removeClass('og-active');
                $('.OG-masthead .og-' + name).addClass('og-active');
            }
        };
    }
});