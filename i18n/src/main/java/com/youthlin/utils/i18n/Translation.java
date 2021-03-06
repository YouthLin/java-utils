package com.youthlin.utils.i18n;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * 翻译工具类.
 * <p>
 * <code>__("str");</code><br>
 * <code>_f("fmt",args...);</code><br>
 * <code>__("msg","domain",args...);</code><br>
 * <code>__("msg",resources,args...);</code><br>
 * <code>_x("str","context");</code><br>
 * <code>_n("single","plural",n);</code><br>
 * <code>_nx("single","plural",n,"context");</code><br>
 * <p>
 * <code>[main]$ xgettext -k__ -k_x:2c,1 -k_n:1,2 -k_nx:3c,1,2  -o resources/Message.pot java/pack/age/Clazz.java --from-code UTF-8</code>
 * <br><code>[main]$ msgfmt --java2 -d resources -r Message -l zh_CN resources\Message_zh_CN.po (--source生成 java 文件)</code>
 * <br><code>addResource("id", ResourceBundle.getBundle("Message"));</code>
 * <p>
 * 也可使用 Poedit 工具抽取待翻译字符串【复数编辑nplurals=2; plural=n == 1 ? 0 : 1;】
 * <p>
 * 使用 {@code getBundle} 方法获取资源。
 *
 * @author YouthLin Chen
 * @see <a href="http://youthlin.com/?p=1315">http://youthlin.com/20161315.html</a>
 * @see MessageFormat
 */
@SuppressWarnings({"WeakerAccess", "unused", "UnusedReturnValue", "SameParameterValue"})
public class Translation {
    public static final ResourceBundle EMPTY_RESOURCE_BUNDLE = new ResourceBundle() {
        @Override
        protected Object handleGetObject(String key) {
            return null;
        }

        @Override
        public Enumeration<String> getKeys() {
            return new Enumeration<String>() {
                @Override
                public boolean hasMoreElements() {
                    return false;
                }

                @Override
                public String nextElement() {
                    return null;
                }
            };
        }
    };
    public static final String DEFAULT_DOMAIN = Translation.class.getName();

    private static ResourceBundle dft = EMPTY_RESOURCE_BUNDLE;
    // 双向队列，添加移除操作多，几乎没有随机访问，因此用 LinkedList
    private static Deque<Pair> resources = new LinkedList<Pair>();
    private static Set<String> domains = new HashSet<String>();
    private static Set<ResourceBundle> catalogs = new HashSet<ResourceBundle>();
    private static boolean verbose = false;

    private static ResourceBundle r = getBundle("com.youthlin.utils.i18n.Message");

    static {
        addResource(DEFAULT_DOMAIN, r);
    }

    //region // add/remove

    /**
     * 注册一个翻译包.
     * <p>
     * 添加一个翻译资源包到队列头部, 翻译时将从队列头部搜索
     *
     * @param domain 命名
     * @param rb     资源包
     * @return true if added
     */
    public static boolean addResource(String domain, ResourceBundle rb) {
        notnull(domain, "domain");
        notnull(rb, "ResourceBundle");
        if (rb.equals(dft) || rb.equals(EMPTY_RESOURCE_BUNDLE)) {
            if (verbose) System.err.println("dft/EMPTY_RESOURCE_BUNDLE is no need to add.");
            return false;//dft 就没有必要添加了
        }
        Pair pair = new Pair(domain, rb);
        if (resources.contains(pair)) {
            return false;
        }
        resources.add(pair);
        domains.add(domain);
        catalogs.add(rb);
        return true;
    }

    /**
     * 移除指定 domain 的资源包
     *
     * @param domain 命名
     * @param rb     资源包
     * @return true if removed
     */
    public static boolean removeResource(String domain, ResourceBundle rb) {
        Pair p = new Pair(domain, rb);
        boolean hasName = false;
        boolean hasCatalog = false;
        if (resources.contains(p)) {
            resources.remove(p);
            //移除后看是否还有
            for (Pair pair : resources) {
                if (pair.name.equals(domain)) {
                    hasName = true;
                }
                if (pair.catalog.equals(rb)) {
                    hasCatalog = true;
                }
            }
            //移除后没有这个 domain 了，那就可以真正把 domain 从 domains 中去掉
            if (!hasName) {
                domains.remove(domain);
            }
            if (!hasCatalog) {
                catalogs.remove(rb);
            }
            return true;
        }
        return false;
    }

    /**
     * 移除 domain 下的所有资源包
     *
     * @param domain 命名
     * @return true if removed
     */
    public static boolean removeResource(String domain) {
        if (domains.contains(domain)) {
            Iterator<Pair> iterator = resources.iterator();
            while (iterator.hasNext()) {
                Pair p = iterator.next();
                if (p.name.equals(domain)) {
                    catalogs.remove(p.catalog);
                    iterator.remove();
                }
            }
            domains.remove(domain);
            return true;
        }
        return false;
    }

    /**
     * 移除指定的资源包，不管命名为什么 domain
     *
     * @param rb 资源包
     * @return true if removed
     */
    public static boolean removeResource(ResourceBundle rb) {
        if (catalogs.contains(rb)) {
            Iterator<Pair> iterator = resources.iterator();
            while (iterator.hasNext()) {
                Pair p = iterator.next();
                if (p.catalog.equals(rb)) {
                    domains.remove(p.name);
                    iterator.remove();
                }
            }
            catalogs.remove(rb);
            return true;
        }
        return false;
    }
    //endregion // add/remove

    //region // __

    /**
     * translate msg to target language.
     *
     * @param msg text to be translated
     * @return translated text
     */
    public static String __(String msg) {
        notnull(msg, "msg");
        for (Pair p : resources) {
            String s = GettextResource2.gettextnull(p.catalog, msg);
            if (s != null) {
                return s;
            }
        }
        return GettextResource2.gettext(dft, msg);
    }

    /**
     * translate msg to target language with Specific format.
     *
     * @param fmt    text(with format)  to be translated
     * @param params params
     * @return translated text
     * @see MessageFormat
     */
    public static String _f(String fmt, Object... params) {
        notnull(fmt, "fmt");
        return format(__(fmt), params);
    }

    /**
     * translate msg to target language.
     *
     * @param msg    text to be translated
     * @param domain DEFAULT_DOMAIN name
     * @param params params
     * @return translated text
     * @see #addResource(String, ResourceBundle)
     */
    public static String __(String msg, String domain, Object... params) {
        notnull(msg, "msg");
        notnull(domain, "domain");
        for (Pair p : resources) {
            if (p.name.equals(domain)) {
                String s = GettextResource2.gettextnull(p.catalog, msg);
                if (s != null) {
                    return format(s, params);
                }
            }
        }
        return format(GettextResource2.gettext(dft, msg), params);
    }

    /**
     * translate msg to target language.
     *
     * @param msg    text to be translated
     * @param rb     ResourceBundle
     * @param params params
     * @return translated text
     * @see #addResource(String, ResourceBundle)
     */
    public static String __(String msg, ResourceBundle rb, Object... params) {
        notnull(msg, "msg");
        notnull(rb, "ResourceBundle");
        String s = GettextResource2.gettextnull(rb, msg);
        if (s != null) {
            return format(s, params);
        }
        return format(GettextResource2.gettext(rb, msg), params);
    }
    //endregion // __

    //region // _x
    public static String _x(String msg, String ctx) {
        notnull(msg, "msg");
        notnull(ctx, "context");
        for (Pair p : resources) {
            String s = GettextResource2.gettextnull(p.catalog, withContext(ctx, msg));
            if (s != null) {
                return s;
            }
        }
        return GettextResource2.pgettext(dft, ctx, msg);
    }

    public static String _fx(String fmt, String ctx, Object... params) {
        notnull(fmt, "fmt");
        notnull(ctx, "context");
        return format(_x(fmt, ctx), params);
    }

    public static String _x(String msg, String ctx, String domain, Object... params) {
        notnull(msg, "msg");
        notnull(ctx, "context");
        notnull(domain, "domain");
        for (Pair p : resources) {
            if (p.name.equals(domain)) {
                String s = GettextResource2.gettextnull(p.catalog, withContext(ctx, msg));
                if (s != null) {
                    return format(s, params);
                }
            }
        }
        return format(GettextResource2.pgettext(dft, ctx, msg), params);
    }

    public static String _x(String msg, String ctx, ResourceBundle rb, Object... params) {
        notnull(msg, "msg");
        notnull(ctx, "context");
        notnull(rb, "ResourceBundle");
        String s = GettextResource2.gettextnull(rb, withContext(ctx, msg));
        if (s != null) {
            return format(s, params);
        }
        return format(GettextResource2.pgettext(rb, ctx, msg), params);
    }
    //endregion // _x

    //region // _n
    public static String _n(String msg, String msg_plural, long n, Object... params) {
        notnull(msg, "msg");
        notnull(msg_plural, "msg_plural");
        for (Pair p : resources) {
            String s = GettextResource2.ngettextnull(p.catalog, msg, n);
            if (s != null) {
                return format(s, params);
            }
        }
        return format(GettextResource2.ngettext(dft, msg, msg_plural, n), params);
    }

    public static String _n(String msg, String msg_plural, String domain, long n, Object... params) {
        notnull(msg, "msg");
        notnull(msg_plural, "msg_plural");
        notnull(domain, "domain");
        for (Pair p : resources) {
            if (p.name.equals(domain)) {
                String s = GettextResource2.ngettextnull(p.catalog, msg, n);
                if (s != null) {
                    return format(s, params);
                }
            }
        }
        return GettextResource2.ngettext(dft, msg, msg_plural, n);
    }

    public static String _n(String msg, String msg_plural, ResourceBundle rb, long n, Object... params) {
        notnull(msg, "msg");
        notnull(msg_plural, "msg_plural");
        notnull(rb, "ResourceBundle");
        String s = GettextResource2.ngettextnull(rb, msg, n);
        if (s != null) {
            return format(s, params);
        }
        return format(GettextResource2.ngettext(dft, msg, msg_plural, n), params);
    }

    //endregion // _n

    //region // _nx
    public static String _nx(String msg, String plural, String ctx, long n, Object... params) {
        notnull(msg, "msg");
        notnull(plural, "plural");
        notnull(ctx, "context");
        for (Pair p : resources) {
            String s = GettextResource2.ngettextnull(p.catalog, withContext(ctx, msg), n);
            if (s != null) {
                return format(s, params);
            }
        }
        return format(GettextResource2.npgettext(dft, ctx, msg, plural, n), params);
    }

    public static String _nx(String msg, String plural, String ctx,
                             String domain, long n, Object... params) {
        notnull(msg, "msg");
        notnull(plural, "plural");
        notnull(ctx, "context");
        notnull(domain, "domain");
        for (Pair p : resources) {
            if (p.name.equals(domain)) {
                String s = GettextResource2.ngettextnull(p.catalog, withContext(ctx, msg), n);
                if (s != null) {
                    return format(s, params);
                }
            }
        }
        return format(GettextResource2.npgettext(dft, ctx, msg, plural, n), params);
    }

    public static String _nx(String msg, String plural, String ctx,
                             ResourceBundle catalog, long n, Object... params) {
        notnull(msg, "msg");
        notnull(plural, "plural");
        notnull(ctx, "context");
        notnull(catalog, "ResourceBundle");
        String s = GettextResource2.ngettextnull(catalog, withContext(ctx, msg), n);
        if (s != null) {
            return format(s, params);
        }
        return format(GettextResource2.npgettext(catalog, ctx, msg, plural, n), params);
    }
    //endregion  // _nx

    //region //util method
    private static String format(String fmt, Object... param) {
        if (param == null || param.length == 0) {
            return fmt;
        }
        return MessageFormat.format(fmt, param);
    }

    private static String withContext(String ctx, String msg) {
        return ctx + GettextResource2.CONTEXT_GLUE + msg;
    }

    private static void notnull(Object o, String parameterName) {
        if (o == null) {
            throw new NullPointerException("The parameter: '" + parameterName + "' should be not null");
        }
    }
    //endregion

    //region //getter and setter
    public static ResourceBundle getDft() {
        return dft;
    }

    public static void setDft(ResourceBundle dft) {
        notnull(dft, "dft");
        Translation.dft = dft;
    }

    public static boolean isVerbose() {
        return verbose;
    }

    public static void setVerbose(boolean verbose) {
        Translation.verbose = verbose;
    }
    //endregion

    public static ResourceBundle getBundle(String baseName) {
        return getBundle(baseName, Locale.getDefault());
    }

    public static ResourceBundle getBundle(String baseName, Locale locale) {
        notnull(baseName, "baseName");
        notnull(locale, "locale");
        /*
         * ResourceBundle.getBundle(baseName, locale) 加载顺序：
         * baseName.locale.class -> baseName.dftLocale.class -> baseName.class
         */
        ResourceBundle bundle = null;
        try {
            bundle = ResourceBundle.getBundle(baseName, locale);
        } catch (MissingResourceException e) {
            if (verbose)
                System.err.println("Can not find resources with locale " + locale + ". using default resources.");
        }
        if (bundle != null && bundle.getLocale().getLanguage().equals(locale.getLanguage())) {
            return bundle;
        }
        return dft;
    }

    public static void main(String[] args) {
        System.out.println(DEFAULT_DOMAIN);
        System.out.println("默认：------------------------");
        print();
        removeResource(DEFAULT_DOMAIN);
        System.out.println("移除后：------------------------");
        print();
        r = getBundle("Message", Locale.FRENCH);
        addResource(DEFAULT_DOMAIN, r);
        System.out.println("Fr：------------------------");
        print();
        removeResource(DEFAULT_DOMAIN, r);
        r = getBundle("Message");
        addResource(DEFAULT_DOMAIN, r);
        System.out.println("默认：------------------------");
        print();
    }

    private static void print() {
        System.out.println(__("Hello, World!"));
        System.out.println(_f("Hello, {0}!", "Lin"));
        System.out.println(_f("Hello, {0}! Now is {1,date} {1,time}", "World", new Date()));
        System.out.println(_x("Post", "a post"));
        System.out.println(_x("Post", "to post"));
        System.out.println(_n("One Comment", "{0} Comments", 1, 1));
        System.out.println(_n("One Comment", "{0} Comments", 3, 3));
        System.out.println(_nx("One Comment", "{0} Comments", "评论", 1, 1));
        System.out.println(_nx("One Comment", "{0} Comments", "注释", 2, 2));
        //removeResource(r);
        System.out.println(_nx("One Comment", "{0} Comments", "注释", 2, 2));
        System.out.println(_nx("One Comment", "{0} Comments", "注释", DEFAULT_DOMAIN, 2, 2));
        System.out.println(_nx("One Comment", "{0} Comments", "注释", r, 2, 2));
        System.out.println(__("xxx"));
    }

    /*私有内部类. 用于添加至 Queue 检查重复.*/
    private static class Pair {
        final String name;
        final ResourceBundle catalog;

        private Pair(String name, ResourceBundle catalog) {
            notnull(name, "name");
            notnull(catalog, "catalog");
            this.name = name;
            this.catalog = catalog;
        }

        /**
         * 重写 equals 和 hashcode 用于 集合类
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return name.equals(pair.name) && catalog.equals(pair.catalog);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + catalog.hashCode();
            return result;
        }
    }
}
