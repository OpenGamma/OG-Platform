$.register_module({
    name: 'og.analytics.DropMenu',
    dependencies: [],
    obj: function () {
        var events = {
                focus: 'dropmenu:focus',
                focused:'dropmenu:focused',
                open: 'dropmenu:open',
                opened: 'dropmenu:opened',
                close: 'dropmenu:close',
                closed: 'dropmenu:closed'
            },
            DropMenu,
            Menu = function () {};
        $.extend(true, Menu.prototype, EventEmitter.prototype);
        Menu.prototype.constructor = Menu;
        Menu.prototype.focus = function () {
            var menu = this;
            return menu.opts[menu.opts.length-1].find('select').first().focus(), menu.opened = true,
                menu.state = 'focused', menu.emitEvent(events.focused, [menu]), menu;
        };
        Menu.prototype.open = function () {
            var menu = this;
            return menu.$dom.menu.show().blurkill(menu.close.bind(menu)), menu.state = 'open', menu.opened = true,
                menu.$dom.title.addClass('og-active'), menu.emitEvent(events.opened, [menu]), menu;
        };
        Menu.prototype.close = function () {
            var menu = this;
            return (menu.$dom.menu ? menu.$dom.menu.hide() : null), menu.state = 'closed', menu.opened = false,
                menu.$dom.title.removeClass('og-active'), menu.emitEvent(events.closed, [menu]), menu;
        };
        Menu.prototype.menu_handler = function (event) {
            var menu = this, elem = $(event.target);
            if (elem.is(menu.$dom.add)) {
                menu.add_handler(); 
                menu.stop(event);
            }
            if (elem.is('.og-icon-delete')) {
                menu.del_handler(elem.closest('.OG-dropmenu-options'));
                menu.stop(event);
            }
            if (elem.is(':checkbox')) {elem.focus();}
        };
        Menu.prototype.title_handler = function () {
            var menu = this;
                menu.opened ? menu.close() : (menu.open(), menu.focus());
        };
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
            for (var i = pos || 0, len = menu.opts.length; i < len;)
                menu.opts[i].data("pos", i).find('.number span').text(i+=1);
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
        /**
         * TODO AG: Must provide Getters/Setters instance properites as these should really be private
         * and not accessible directly via the instance.
         */
        return DropMenu = function (config) {
            var m = new Menu(), tmpl = config.tmpl, dummy_s = '<div>', data = config.data || {};
            m.state = 'closed';
            m.opened = false;
            m.data = data;
            m.$dom = {};
            m.$dom.cntr = config.$cntr.html($((Handlebars.compile(tmpl))(data)));
            m.$dom.title = $('.og-option-title', m.$dom.cntr);
            m.$dom.title_prefix = $(dummy_s);
            m.$dom.title_infix = $(dummy_s);
            m.$dom.menu = $('.OG-analytics-form-menu', m.$dom.cntr);
            m.$dom.menu_actions = $('.OG-dropmenu-actions', m.$dom.menu);
            m.$dom.opt = $('.OG-dropmenu-options', m.$dom.menu);
            m.$dom.opt.data("pos", ((m.opts = []).push(m.$dom.opt), m.opts.length-1));
            m.$dom.add = $('.OG-link-add', m.$dom.menu);
            m.$dom.opt_cp = m.$dom.opt.clone(true);
            m.addListener(events.open, m.open.bind(m))
                .addListener(events.close, m.close.bind(m))
                .addListener(events.focus, m.focus.bind(m));
            return m;
        };
    }
});