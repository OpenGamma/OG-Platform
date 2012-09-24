
$.register_module({
    name: 'og.analytics.AggregatorsMenu',
    dependencies: ['og.analytics.DropMenu'],
    obj: function () { 
        return function (config) {
            var menu = new og.analytics.DropMenu(config), $dom = menu.$dom, opts = menu.opts, data = menu.data,
                ag_selection, $ag_selection = $('.aggregation-selection', $dom.title), 
                default_sel_txt = 'select aggregation type...';
                $dom.title_prefix.append('<span>Aggregated by</span>');
                $dom.title_infix.append('<span>then</span>');
                (ag_selection = []).push($dom.title_prefix.html());
                select_handler = function (elem) {
                    var parent = elem.closest('.OG-dropmenu-options'), pos = parent.data('pos'), text, 
                        val = elem.val(), entry = ag_selection.pluck('pos').indexOf(pos);
                    if (val === default_sel_txt) remove_entry(pos);
                    else if (entry !== -1) ag_selection[entry].val = val;
                    else add_infix.call(ag_selection, ag_selection.push({pos:pos, val:val}));
                    console.log(ag_selection);
                },
                add_infix = function (array) {
                    var arr = this;
                    if (arr.length-1 > 1) arr.splice(-1, 0, $dom.title_infix.html());
                },
                replace_entry = function (obj) {

                },
                remove_entry = function (pos) {
                    var entry;
                    if (ag_selection.length-1 == 1) return ag_selection.pop();
                    entry = ag_selection.pluck('pos').indexOf(pos);
                    if (entry) ag_selection.splice(entry-1, 2);
                },
                menu_handler = function (event) {
                    var elem = $(event.target);
                    if (elem.is(menu.$dom.add)) {
                        if (data.length === opts.length) return;
                        menu.add_handler(); 
                        menu.stop(event);
                    }
                    if (elem.is('.og-icon-delete')) {
                        menu.del_handler(elem.closest('.OG-dropmenu-options'));
                        menu.stop(event);
                    }
                    if (elem.is(':checkbox')) elem.focus();
                    if (elem.is('select')) select_handler(elem);
                };
            $dom.title.on('click', menu.title_handler.bind(menu));
            $dom.menu.on('click', menu_handler).on('change', menu_handler);
            return menu;
        };
    }
});