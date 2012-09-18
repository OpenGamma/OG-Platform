
$.register_module({
    name: 'og.analytics.dropmenu',
    dependencies: [],
    obj: function () { 
        return function (config) {
            var Dropmenu = this, Menu, tmpl = config.tmpl, data = config.data || {}, title_click, add_click,
                $cntr = config.$cntr.html($((Handlebars.compile(tmpl))(data))),
                $title = $('.og-option-title', $cntr).on('click', title_click = function (event) {
                    event.stopPropagation();
                    Dropmenu.opened ? Dropmenu.close() : Dropmenu.open().focus();
                }), $menu = $('.OG-analytics-form-menu', $cntr), $menu_actions = $('.OG-dropmenu-actions', $menu), 
                $add = $('.OG-link-add', $menu).on('click', add_click = function (event) {
                    $menu_actions.before($opts.clone()[0]);
                    event.stopPropagation();
                }), $opts = $('.OG-dropmenu-options', $menu), events = {
                    focus: 'dropmenu:focus',
                    focused:'dropmenu:focused',
                    open: 'dropmenu:open',
                    opened: 'dropmenu:opened',
                    close: 'dropmenu:close',
                    closed: 'dropmenu:closed'
                };
            return Menu = function () {
                var menu = this;
                return menu.state = 'closed', menu.opened = false,
                    menu.focus = function () {
                        return $menu.find('select').first().focus(), menu.opened = true,
                            menu.state = 'focused', menu.emitEvent(events.focused, [menu]), menu;
                    },
                    menu.open = function () {
                        return $menu.show(), menu.state = 'open', menu.opened = true,
                            $title.addClass('og-active'), menu.emitEvent(events.opened, [menu]), menu;
                    },
                    menu.close = function () {
                        return ($menu ? $menu.hide() : null), menu.state = 'closed', menu.opened = false,
                            $title.removeClass('og-active'), menu.emitEvent(events.closed, [menu]), menu;
                    },
                    menu.addListener(events.open, menu.open),
                    menu.addListener(events.close, menu.close),
                    menu.addListener(events.focus, menu.focus),
                    menu;
            }, Menu.prototype = EventEmitter.prototype, Dropmenu = new Menu();
        }
    }
});