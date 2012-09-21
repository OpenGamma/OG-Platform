
$.register_module({
    name: 'og.analytics.DropMenu',
    dependencies: [],
    obj: function () {
        var module = this, events = {
                focus: 'dropmenu:focus',
                focused:'dropmenu:focused',
                open: 'dropmenu:open',
                opened: 'dropmenu:opened',
                close: 'dropmenu:close',
                closed: 'dropmenu:closed'
            },
            Menu = function (config) {
                var menu = this, tmpl = config.tmpl, data = config.data || {};
                return menu.opts, menu.state = 'closed', menu.opened = false, menu.$dom = {}, menu.data = data,
                    menu.$dom.cntr = config.$cntr.html($((Handlebars.compile(tmpl))(data))),
                    menu.$dom.title = $('.og-option-title', menu.$dom.cntr).on('click', menu.title_handler(menu)),
                    menu.$dom.menu = $('.OG-analytics-form-menu', menu.$dom.cntr),
                    menu.$dom.menu_actions = $('.OG-dropmenu-actions', menu.$dom.menu),
                    menu.$dom.opt = $('.OG-dropmenu-options', menu.$dom.menu),
                    menu.$dom.opt.data("pos", ((menu.opts = []).push(menu.$dom.opt), menu.opts.length-1)),
                    menu.$dom.add = $('.OG-link-add', menu.$dom.menu),
                    menu.$dom.opt_cp = menu.$dom.opt.clone(true),
                    menu.addListener(events.open, menu.open),
                    menu.addListener(events.close, menu.close),
                    menu.addListener(events.focus, menu.focus),
                    menu; 
            };
        Menu.prototype = EventEmitter.prototype;
        Menu.prototype.focus = function () {
            var menu = this;
            return menu.opts[menu.opts.length-1].find('select').first().focus(), menu.opened = true, menu.state = 'focused',
                menu.emitEvent(events.focused, [menu]), menu;
        };
        Menu.prototype.open = function () {
            var menu = this;
            return menu.$dom.menu.show().blurkill(menu.close), menu.state = 'open', menu.opened = true,
                menu.$dom.title.addClass('og-active'), menu.emitEvent(events.opened, [menu]), menu;
        };
        Menu.prototype.close = function () {
            var menu = this;
            return (menu.$dom.menu ? menu.$dom.menu.hide() : null), menu.state = 'closed', menu.opened = false,
                menu.$dom.title.removeClass('og-active'), menu.emitEvent(events.closed, [menu]), menu;
        };
        Menu.prototype.title_handler = function (context) {
            var menu = context;
            return function (event) {
                menu.opened ? menu.close() : (menu.open(), menu.focus());
            }
        }
        Menu.prototype.add_handler = function () {
            var menu = this, opt, len = menu.opts.length;
            return opt = menu.$dom.opt_cp.clone(true).data("pos", menu.opts.length), menu.opts.push(opt), 
                    menu.$dom.add.focus(), menu.opts[len].find('.number span').text(menu.opts.length), 
                    menu.$dom.menu_actions.before(menu.opts[len]);
        };
        Menu.prototype.del_handler = function (elem) {
            var menu = this, data = elem.data();
            if (menu.opts.length === 1) return;
            menu.opts.splice(data.pos, 1);
            elem.remove();
            menu.update_opt_nums(data.pos);
            if (data.pos < menu.opts.length) menu.opts[data.pos].find('select').first().focus();
            else menu.opts[data.pos-1].find('select').first().focus();
        };
        Menu.prototype.update_opt_nums = function (pos) {
            var menu = this;
            for (var i = pos || 0, len = menu.opts.length; i < len;) {
                menu.opts[i].data("pos", i).find('.number span').text(i+=1);
            }   
        };
        Menu.prototype.stop = function (event) {
            event.stopPropagation();
            event.preventDefault();
        };
        Menu.prototype.status = function () {
            var menu = this;
            return menu.state;
        };
        Menu.prototype.is_opened = function () {
            var menu = this;
            return menu.opened;
        };
        return function (config) {
            return new Menu(config);
        }
    }
});