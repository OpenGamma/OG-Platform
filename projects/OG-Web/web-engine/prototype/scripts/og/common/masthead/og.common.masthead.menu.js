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
                var build_menu = function (list) {
                    var menu_html = "<td>";
                    list.forEach(function (entry) {
                        menu_html += '<header>' + entry.group + '</header><ul>';
                        entry.types.forEach(function (type) {
                            menu_html += '<li><a href="admin.ftl#/configs/filter=true/type=' + type.value + '">'
                                + type.name + '</a></li>';
                        });
                        menu_html += '</ul>';
                    });
                    menu_html += "</td>";
                    return menu_html;
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
                og.api.rest.configs.get({meta: true, cache_for: 15 * 1000}).pipe(function (result) {
                    // remove the first half of groups and assign to 'left', the 'right' will then what is left
                    var groups = result.data.groups, left = groups.splice(0, Math.floor(groups.length / 2));
                    menu_html = '<table><tr>';
                    menu_html += build_menu(left);
                    menu_html += build_menu(groups);
                    menu_html += '</tr></table>';
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