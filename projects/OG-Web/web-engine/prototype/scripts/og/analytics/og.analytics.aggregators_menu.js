/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.AggregatorsMenu',
    dependencies: ['og.analytics.DropMenu'],
    obj: function () {
        return function (config) {
            if (!config) return og.dev.warn('og.analytics.AggregatorsMenu: Missing param [config] to constructor.');

            if (!('cntr' in config) || !config.cntr)
                return og.dev.warn('og.analytics.AggregatorsMenu: Missing param key [config.cntr] to constructor.');

            if (!('tmpl' in config) || !config.tmpl)
                return og.dev.warn('og.analytics.AggregatorsMenu: Missing param key [config.tmpl] to constructor.');

            if (typeof config.tmpl !== 'string') return og.dev.warn(
                'og.analytics.AggregatorsMenu Invalid type param key [config.tmpl] to constructor; expected "string"'
            );

            if (!('data' in config) || !config.data || !config.data.length)
                return og.dev.warn('og.analytics.AggregatorsMenu: Missing param key [config.data] to constructor.');

            // Private
            var menu = new og.analytics.DropMenu({
                    $cntr: config.cntr,
                    data: config.data,
                    tmpl: config.tmpl
                }),
                $dom, data, query = [], sel_val, sel_pos, $parent, $query, $select, $checkbox,
                default_sel_txt = 'select aggregation...', default_query_text = 'Default', del_s = '.og-icon-delete',
                options_s = '.OG-dropmenu-options', select_s = 'select', checkbox_s = '.og-option :checkbox';

            var add_handler = function () {
                if (menu.data.length === menu.opts.length) return;
                menu.add_handler();
            };
            var checkbox_handler = function (entry) {
                if ($checkbox[0].checked && ~entry) query[entry].required_field = true;
                else if (!$checkbox[0].checked && ~entry) query[entry].required_field = false;
                $checkbox.focus();
            };
            var delete_handler = function (entry) {
                if (menu.opts.length === 1) {
                    if ($checkbox) $checkbox[0].disabled = true;
                    $query.text(default_query_text);
                    $select.val(default_sel_txt).focus();
                    return remove_entry();
                }
                if($select !== undefined) menu.delete_handler($parent);
                for (var i = ~entry ? entry : sel_pos, len = query.length; i < len; query[i++].pos-=1);
                if (~entry) {
                    remove_entry(entry);
                    display_query();
                }
            };
            var display_query = function () {
                if(query.length) {
                    var i = 0, arr = [], query_val;
                    query.sort(menu.sort_opts).forEach(function (entry) { // revisit the need for sorting this..
                        if (i > 0) arr[i++] = $dom.toggle_infix.html() + " ";
                        arr[i++] = entry;
                    });
                    query_val = arr.reduce(function (a, v) {return a += v.val ? v.val : v;}, '');
                    $query.html(query_val);
                } else $query.text(default_query_text);
            };
            var init = function (conf) {
                $dom = menu.$dom;
                if ($dom) {
                    $query = $('.aggregation-selection', $dom.toggle);
                    $dom.toggle_prefix.append('<span>Aggregated by</span>');
                    if ($dom.menu) {
                        $dom.menu
                            .on('click', 'input, button, div.og-icon-delete, a.OG-link-add', menu_handler)
                            .on('change', 'select', menu_handler);
                        if (conf.opts) menu.replay_query(conf.opts);
                    }
                }
            };
            var init_menu_elems = function (index) {
                $parent = menu.opts[index];
                $select = $parent.find(select_s);
                $checkbox = $parent.find(checkbox_s);
            };
            var menu_handler = function (event) {
                var $elem = $(event.srcElement || event.target), entry;
                    $parent = $elem.parents(options_s);
                    $select = $parent.find(select_s);
                    $checkbox = $parent.find(checkbox_s);
                    sel_val = $select.val();
                    sel_pos = $parent.data('pos');
                    entry = query.pluck('pos').indexOf(sel_pos);
                if ($elem.is(menu.$dom.add)) return menu.stop(event), add_handler();
                if ($elem.is(del_s)) return menu.stop(event), delete_handler(entry);
                if ($elem.is($checkbox)) return checkbox_handler(entry);
                if ($elem.is($select)) return select_handler(entry);
                if ($elem.is('button')) return menu.button_handler($elem.text());
            };
            var remove_entry = function (entry) {
                if (query.length === 1) return query.length = 0;
                query.splice(entry, 1);
            };
            var remove_orphans = function () {
                for (var i = menu.opts.length-1; i >= 0; i-=1) {
                    if (menu.opts.length === 1) break;
                    var option = menu.opts[i];
                    if ($(select_s, option).val() === default_sel_txt) menu.delete_handler(option);
                }
            };
            var reset_query = function () {
                return $query.text(default_query_text), $select.val(default_sel_txt).focus(), remove_entry();
            };
            var select_handler = function (entry) {
                    if (sel_val === default_sel_txt) {
                        remove_entry(entry);
                        if ($checkbox) $checkbox[0].disabled = true;
                        if (query.length === 0) return $query.html(default_query_text);
                    } else if (~entry) query[entry].val = sel_val;
                    else {
                        query.splice(sel_pos, 0, {pos:sel_pos, val:sel_val, required_field:false});
                        $checkbox[0].disabled = false;
                    }
                display_query();
            };

            // Public
            menu.destroy = function () {};
            menu.get_query = function () {
                remove_orphans();
                return query.pluck('val');
            };
            menu.replay_query = function (conf) {
                if (!conf && !conf.aggregators || !$.isArray(conf.aggregators)) return;
                menu.opts.forEach(function (option) {
                    option.remove();
                });
                menu.opts.length = 0;
                query = [];
                conf.aggregators.forEach(function (entry, index) {
                    if (menu.opts.length < conf.aggregators.length) add_handler();
                    init_menu_elems(index);
                    $select.val(entry.val);
                    $checkbox[0].disabled = false;
                    query.splice(index, 0, {pos: index, val: entry.val, required_field: entry.required_field});
                    display_query();
                });
            };
            menu.reset_query = function () {
                for (var i = menu.opts.length-1; i >=0; i-=1) {
                    if (menu.opts.length === 1) {
                        menu.opts[i].val(default_sel_txt);
                        break;
                    }
                    init_menu_elems(i);
                    delete_handler(i);
                }
                return init_menu_elems(0), reset_query();
            };
            return init(config), menu;
        };
    }
});