/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form.AggregatorsMenu',
    dependencies: [],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var AggregatorsMenu = function (config) {
            if (!config) return og.dev.warn('og.analytics.AggregatorsMenu: Missing param [config] to constructor.');

            if (!(config.hasOwnProperty('form')) || !(config.form instanceof og.common.util.ui.Form))
                return og.dev.warn(
                    'og.analytics.AggregatorsMenu: Missing or invalid param key [config.form] to constructor.'
                );

            // Private
            var block = this, form = config.form, query = [], sel_val, sel_pos, $parent, default_aggregation = {idx:0},
                initialized = false, $select, default_sel_txt = 'select aggregation...', del_s = '.og-icon-delete',
                options_s = '.OG-dropmenu-options', select_s = 'select', checkbox_s = '.og-option :checkbox',
                aggregators = config.aggregators && config.aggregators.length ?
                                config.aggregators :
                                [default_aggregation];

            var add_handler = function (obj) {
                add_row_handler(obj).html(function (html) {
                    menu.add_handler($(html));
                    select_handler(menu.opts.length-1, true);
                });
            };

            var add_row_handler = function (obj) {
                return new form.Block({
                    module: 'og.analytics.form_aggregation_row_tash',
                    generator: function (handler, tmpl, data) {
                        og.api.rest.aggregators.get().pipe(function (resp) {
                            if (resp.error) return og.dev.warn('og.analytics.AggregatorsMenu: ' + resp.error);
                            data.aggregators = resp.data.map(function (entry) {
                                return {text: entry, selected: obj === entry};
                            });
                            data.idx = obj.idx + 1;
                            handler(tmpl(data));
                        });
                    }
                });
            };

            var delete_handler = function (entry) {
                if (menu.opts.length === 1) {
                    $select.val(default_sel_txt).focus();
                    return remove_entry();
                }
                if($select !== undefined) menu.delete_handler($parent);
                for (var i = ~entry ? entry : sel_pos, len = query.length; i < len; query[i++].pos-=1);
                if (~entry) {
                    remove_entry(entry);
                }
            };

            var serialize = function () {
                var q = query.pluck('val').filter(function (entry) {
                    return entry !== default_sel_txt;
                });
                return remove_orphans(), q;
            };

            var init = function () {
                menu = new og.analytics.form.DropMenu({cntr: $('.OG-analytics-form .og-aggregation')});
                if (menu.$dom) {
                    $query = $('.aggregation-selection', menu.$dom.toggle);
                    if (menu.$dom.menu)
                        menu.$dom.menu
                            .on('click', 'input, button, div.og-icon-delete, a.OG-link-add', menu_handler)
                            .on('change','select', menu_handler)
                            .on('keypress', 'select.source', function (event) {
                                if (event.keyCode === 13) return form.submit();
                            });
                    menu.opts.forEach(function (entry, idx) { select_handler(idx, true); });
                    og.common.events.on('aggregators:dropmenu:open', function() {menu.fire('dropmenu:open', this);});
                    og.common.events.on('aggregators:dropmenu:close', function() {menu.fire('dropmenu:close', this);});
                    og.common.events.on('aggregators:dropmenu:focus', function() {menu.fire('dropmenu:focus', this);});
                    menu.fire('initialized', [initialized = true]);
                }
            };

            var menu_handler = function (event) {
                var $elem = $(event.srcElement || event.target), entry;
                    $parent = $elem.parents(options_s);
                    $select = $parent.find(select_s);
                    sel_val = $select.val();
                    sel_pos = $parent.data('pos');
                    entry = query.pluck('pos').indexOf(sel_pos);
                if ($elem.is(menu.$dom.add)) return menu.stop(event), add_handler(default_aggregation);
                if ($elem.is(del_s)) return menu.stop(event), delete_handler(entry);
                if ($elem.is($select)) return select_handler(entry);
                if ($elem.is('button')) return menu.button_handler($elem.text()), menu.stop(event), false;
            };

            var remove_entry = function (entry) {
                if (query.length === 1) return query.length = 0;
                if (~entry) query.splice(entry, 1);
            };

            var remove_orphans = function () {
                for (var i = menu.opts.length-1; i >= 0; i-=1) {
                    if (menu.opts.length === 1) break;
                    var option = menu.opts[i];
                    if ($(select_s, option).val() === default_sel_txt) menu.delete_handler(option);
                }
            };

            var reset = function () {
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

            var select_handler = function (entry, preload) {
                if (!menu.opts[entry]) return;
                var sel_pos = menu.opts[entry].data('pos'), select = $(select_s, menu.opts[entry]),
                    sel_val = select.val(), idx = query.pluck('pos').indexOf(sel_pos);
                if (sel_val === default_sel_txt && !preload) {
                    remove_entry(idx);
                    if (query.length === 0) $query.html(default_sel_txt);
                }
                else if (~idx) query[entry].val = sel_val;
                else query.splice(sel_pos, 0, {pos:sel_pos, val:sel_val, required_field:false});
            };

            form.Block.call(block, {
                data: { aggregators:[] },
                module: 'og.analytics.form_aggregation_tash',
                children: aggregators.map(add_row_handler),
                processor: function (data) {
                    data.aggregators = serialize();
                }
            });
            form.on('form:load', init);
        };

        AggregatorsMenu.prototype = new Block;
        return AggregatorsMenu;
    }
});