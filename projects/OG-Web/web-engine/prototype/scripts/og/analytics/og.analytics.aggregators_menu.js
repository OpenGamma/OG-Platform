
$.register_module({
    name: 'og.analytics.AggregatorsMenu',
    dependencies: ['og.analytics.DropMenu'],
    obj: function () { 
        return function (config) {
            var menu = new og.analytics.DropMenu(config), $dom = menu.$dom, opts = menu.opts, data = menu.data,
                ag_opts = [], $ag_selection = $('.aggregation-selection', $dom.title),
                default_sel_txt = 'select aggregation type...', options_s = '.OG-dropmenu-options';
                $dom.title_prefix.append('<span>Aggregated by</span>');
                $dom.title_infix.append('<span>then</span>');
                select_handler = function (elem) {
                    var parent = elem.parents(options_s), pos = parent.data('pos'), text, val = elem.val(),
                        entry = ag_opts.pluck('pos').indexOf(pos);
                    if (val === default_sel_txt) remove_entry(entry);
                    else if (entry !== -1) replace_entry(entry, val);
                    else ag_opts[pos] = {pos:pos, val:val};
                    process_ag_opts();
                },
                checkbox_handler = function (elem) {
                    /*
                     var parent = elem.closest(options_s), pos = parent.data('data'),
                        entry = ag_opts.pluck('pos').indexOf(pos);
                    if (elem.checked && entry === -1) ag_opts[pos].required_field = true; // probably use add_infix here!?
                    else if (elem.checked && entry !== -1) ag_opts[entry].required_field = true;
                    */
                    elem.focus();
                },
                process_ag_opts = function () {
                    var arr = [], i = 0, query;
                    ag_opts.forEach(function (entry) { arr[i++] = entry; });
                    query = arr.reduce(function (a, v, j) {
                        return a += (j%2) ? $dom.title_infix.html() + " " + v.val : v.val;
                    }, '');
                    $ag_selection.html(query);
                },
                replace_entry = function (pos, val) {
                    ag_opts[pos].val = val;
                },
                remove_entry = function (pos) {
                    if (ag_opts.length === 1) return $ag_selection.text(default_sel_txt), ag_opts = [];
                    ag_opts.splice(pos, 1);
                },
                add_infix = function (array) {
                    var arr = this;
                    if (arr.length > 1) arr.splice(-1, 0, $dom.title_infix.html());
                },
                menu_handler = function (event) {
                    var elem = $(event.target);
                    if (elem.is(menu.$dom.add)) {
                        if (data.length === opts.length) return;
                        menu.add_handler(); 
                        menu.stop(event);
                    }
                    if (elem.is('.og-icon-delete')) {
                        menu.del_handler(elem.closest(options_s));
                        menu.stop(event);
                    }
                    if (elem.is(':checkbox')) checkbox_handler(elem);
                    if (elem.is('select')) select_handler(elem);
                };
            $dom.title.on('click', menu.title_handler.bind(menu));
            $dom.menu.on('click', menu_handler).on('change', menu_handler);
            return menu;
        };
    }
});