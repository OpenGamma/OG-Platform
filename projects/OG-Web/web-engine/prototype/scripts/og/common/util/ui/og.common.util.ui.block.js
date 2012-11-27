/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */

$.register_module({
    name: 'og.common.util.ui.Block',
    dependencies: ['og.api.text'],
    obj: function () {
        var module = this, STALL = 500 /* 500ms */, api_text = og.api.text;
        /** @private */
        var set_template = function (name, html) {return !name || !html ? false : Handlebars.compile(html);};
        /**
         * creates a Block instance
         * @name Block
         * @constructor
         * @param {Form} form parent Form instance
         * @param {Object} config configuation options for Block instance
         * @param {String} config.module the Handlebars template name (optional)
         * @param {String} config.content static content that will populate the Block (optional)
         * @param {Function} config.generator function that will receive a handler into which it can send Block contents
         * whenever it is ready, useful for populating a block with async content (optional)
         * @param {Object} config.extras values plugged into the Handlerbars template (optional)
         * @param {String} config.wrap simple Handlebars template with a blob named "html" used to wrap children
         * @property {Array} children collection of children within a Block instance, each child is itself a Block
         */
        var Block = function (form, config) {
            var block = this, config = block.config = config || {};
            block.children = config.children || [];
            block.form = form;
            if (block.template) return; // good to go if template is supplied by prototype
            block.template = 'loading';
            $.when(config.module ? api_text({module: config.module}) : void 0)
                .then(function (result) {block.template = set_template(config.module, result);});
        };
        /**
         * generates the HTML for a Block instance and all of its children and calls a supplied handler with that string
         * @param {Function} handler the handler that receives the HTML string
         * @type Block
         * @returns {Block} reference to current Block instance
         */
        Block.prototype.html = function (handler) {
            var block = this, template = block.template, total = block.children.length, done = 0, result = [],
                generator = block.config.generator;
            /** @private */
            var internal_handler = function () {
                handler(template ? template($.extend(
                    result.reduce(function (acc, val, idx) {return acc['item_' + idx] = val, acc;}, {}),
                    block.config.extras
                )) : wrap(result.join('')));
            };
            /** @private */
            var wrap = function (html) {
                return block.config.wrap ? Handlebars.compile(block.config.wrap)({html: html}) : html;
            };
            if (block.config.content) return handler(block.config.content), block;
            if (template === 'loading') return setTimeout(function () {block.html(handler);}, STALL), block;
            if (generator) return generator(handler, template), block;
            if (!total) return internal_handler(), block;
            block.children.forEach(function (val, idx) {
                if (val.html)
                    return val.html(function (html) {(result[idx] = html), (total === ++done) && internal_handler();});
                result[idx] = val;
                if (total === ++done) internal_handler();
            });
            return block;
        };
        /**
         * simulates a form:load event, only used when new blocks are added and need to be set up
         * @type Block
         * @returns {Block} reference to current Block instance
         */
        Block.prototype.load = function () {
            var block = this, form = block.form;
            if (form.events['form:load']) // mimic a form load event
                form.events['form:load'].forEach(function (val) {if (val.origin === block) val.handler();});
            block.children.forEach(function (child) {if (child.load) child.load();});
            return block;
        };
        /**
         * delegates off functionality to current Block instance's parent {@link Form#off}
         * @type Block
         * @returns {Block} reference to current Block instance
         */
        Block.prototype.off = function () {
            var block = this;
            return block.form ? block.form.off.apply(block, Array.prototype.slice.call(arguments)) : block;
        };
        /**
         * delegates on functionality to current Block instance's parent {@link Form#on}
         * @type Block
         * @returns {Block} reference to current Block instance
         */
        Block.prototype.on = function () {
            var block = this;
            return block.form ? block.form.on.apply(block, Array.prototype.slice.call(arguments)) : block;
        };
        /** @ignore */
        Block.prototype.process = function (data, errors) {
            var block = this, processor = block.config.processor;
            block.children.forEach(function (child) {if (child.process) child.process(data, errors);});
            try {if (processor) processor(data);} catch (error) {errors.push(error);}
        };
        Block.prototype.template = null;
        return Block;
    }
});