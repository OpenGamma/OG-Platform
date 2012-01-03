from dexy.filters.templating_filters import JinjaFilter

class OpenGammaFilter(JinjaFilter):
    ALIASES = ['ogjinja']

    def split_package_class(self, class_name):
        package_name = ".".join(class_name.split(".")[0:-1])
        unqualified_class_name = class_name.split(".")[-1]
        return package_name, unqualified_class_name

    def javadoc_url(self, class_name):
        """
        Returns a URL to the javadoc for the fully qualified class_name.
        """
        # domain = "docs-static.opengamma.com"
        domain = "204.236.211.242"
        og_version = self.artifact.controller_args['globals']['OG_VERSION']

        return "http://%s/%s/java/javadocs/%s.html" % (domain, og_version, class_name.replace(".", "/"))

    def javadoc_href(self, class_name, link_text=None, shorten_class_name=True):
        """
        Returns a LaTeX href which links to the javadoc for the fully qualified class_name.
        If shorten_class_name is True, the unqualified class name will be displayed.
        """
        url = self.javadoc_url(class_name)
        if link_text:
            display_name = link_text
        elif shorten_class_name:
            display_name = class_name.split(".")[-1]
        else:
            display_name = class_name

        return "\href{%s}{%s}" % (url, display_name)
