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

            if (!(config.hasOwnProperty('form')) || !(config.form instanceof og.common.util.ui.Form))
                return og.dev.warn('og.analytics.AggregatorsMenu: Missing param key [config.form] to constructor.');

            // Private
            var default_conf = {
                    form: config.form,
                    selector: '.og-aggregation',
                    tmpl: 'og.analytics.form_aggregation_tash',
                    children: [
                        new og.common.util.ui.Dropdown({
                            form: config.form, resource: 'aggregators', value: '', placeholder: 'select aggregation...'
                        })
                    ]
                },
                events = {
                    reset:'reset',
                    replay:'replay'
                },
                $dom, query = [], sel_val, sel_pos, $parent, $query, form = config.form, initialized = false,
                $select, $checkbox, default_sel_txt = 'select aggregation...', default_query_text = 'Default',
                del_s = '.og-icon-delete', options_s = '.OG-dropmenu-options', select_s = 'select',
                checkbox_s = '.og-option :checkbox', tmpl_menu = '', tmpl_toggle = '', menu;

            var add_handler = function (opts) {
                new og.common.util.ui.Dropdown({
                    form: config.form, resource: 'aggregators',
                    value: (opts && opts.hasOwnProperty('val') ? opts.val : ''), placeholder: 'select aggregation...'
                }).html(function (html) {
                    var idx, $elem = menu.$dom.opt_cp.clone(true); $elem.find('td.aggregation').html(html);
                    menu.add_handler($elem); idx = menu.opts.length-1;
                    menu.opts[idx].find('.og-option input[type=checkbox]').attr('name', 'required_fields.'+idx);
                });
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

            var init = function () {
                $dom = menu.$dom;
                if ($dom) {
                    $dom.toggle_prefix.append('<span>Aggregated by</span>');
                    if ($dom.menu) {
                        $query = $('.aggregation-selection', $dom.toggle);
                        $dom.menu
                            .on('mousedown', 'input, button, div.og-icon-delete, a.OG-link-add', menu_handler)
                            .on('change', 'select', menu_handler);
                    }
                }
                menu.fire('initialized', [initialized = true]);
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
                return $query.text(default_query_text), remove_entry();
            };

            var select_handler = function (entry) {
                if (sel_val === "") {
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

            return menu = new og.analytics.DropMenu(default_conf, init),

            // Public
            menu.replay = function (conf) {
                var replay_opts = function () {
                    if (!conf || !conf.hasOwnProperty('aggregators')) return;
                    var aggregators = conf.aggregators;
                    if (!$.isArray(aggregators) || !aggregators.length) return;
                    menu.opts.forEach(function (option) { option.remove(); });
                    menu.opts.length = 0;
                    query = [];
                    conf.aggregators.forEach(function (entry, index) {
                        if (menu.opts.length < conf.aggregators.length) add_handler({ val: entry, idx: index });
                        query.splice(index, 0, {pos: index, val: entry, required_field: true});
                    });
                    display_query();
                };
                if (!initialized) menu.addListener('initialized', replay_opts); else replay_opts();
            },

            menu.reset = function () {
                menu.opts.forEach(function (option) { option.remove(); });
                menu.opts.length = 0;
                query = [];
                return add_handler(), reset_query();
            },

            menu.get_query = function () {
                return remove_orphans(), query.pluck('val');
            },

            menu;
        };
    }
});