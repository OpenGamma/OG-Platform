
$.register_module({
    name: 'og.analytics.AggregatorsMenu',
    dependencies: ['og.analytics.DropMenu'],
    obj: function () { 
        return function (config) {
            var menu = new og.analytics.DropMenu(config), $dom = menu.$dom, opts = menu.opts, data = menu.data,
                title_selection, $title_selection = $('.aggregation-selection', $dom.title);
                (title_selection = []).push($dom.title_start.html());
                select_handler = function (elem) {
                    var text;
                    title_selection.push(elem.val());
                    title_selection.push($dom.title_conjunction.html());
                    text = title_selection.join(' ');
                    $title_selection.html(text);
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
        }
    }
});