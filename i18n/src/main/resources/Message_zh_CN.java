/* Automatically generated by GNU msgfmt.  Do not modify!  */
public class Message_zh_CN extends java.util.ResourceBundle {
    private static final java.lang.Object[] table;

    static {
        java.lang.Object[] t = new java.lang.Object[38];
        t[0] = "";
        t[1] = "Project-Id-Version: \nReport-Msgid-Bugs-To: \nPOT-Creation-Date: 2017-01-30 14:28+0800\nPO-Revision-Date: 2017-01-30 14:34+0800\nLanguage-Team: \nMIME-Version: 1.0\nContent-Type: text/plain; charset=UTF-8\nContent-Transfer-Encoding: 8bit\nPlural-Forms: nplurals=2; plural=n == 1 ? 0 : 1;\nX-Generator: Poedit 1.8.9\nLast-Translator: YouthLin Chen <root@youthlin.com>\nLanguage: zh_CN\n";
        t[12] = "to post\u0004Post";
        t[13] = "\u53d1\u8868";
        t[14] = "\u8bc4\u8bba\u0004One Comment";
        t[15] = new java.lang.String[]{"1 \u6761\u8bc4\u8bba", "{0} \u6761\u8bc4\u8bba"};
        t[16] = "Hello,World";
        t[17] = "\u4e16\u754c\uff0c\u4f60\u597d";
        t[18] = "a post\u0004Post";
        t[19] = "\u6587\u7ae0";
        t[22] = "\u6ce8\u91ca\u0004One Comment";
        t[23] = new java.lang.String[]{"1 \u6761\u6ce8\u91ca", "{0} \u6761\u6ce8\u91ca"};
        t[28] = "One Comment";
        t[29] = new java.lang.String[]{"1 \u6761\u8bc4\u8bba", "{0} \u6761\u8bc4\u8bba"};
        table = t;
    }

    public static final java.lang.String[] get_msgid_plural_table() {
        return new java.lang.String[]{"{0} Comments", "{0} Comments", "{0} Comments"};
    }

    public java.lang.Object lookup(java.lang.String msgid) {
        int hash_val = msgid.hashCode() & 0x7fffffff;
        int idx = (hash_val % 19) << 1;
        java.lang.Object found = table[idx];
        if (found != null && msgid.equals(found))
            return table[idx + 1];
        return null;
    }

    public java.lang.Object handleGetObject(java.lang.String msgid) throws java.util.MissingResourceException {
        java.lang.Object value = lookup(msgid);
        return (value instanceof java.lang.String[] ? ((java.lang.String[]) value)[0] : value);
    }

    public java.util.Enumeration getKeys() {
        return
                new java.util.Enumeration() {
                    private int idx = 0;

                    {
                        while (idx < 38 && table[idx] == null) idx += 2;
                    }

                    public boolean hasMoreElements() {
                        return (idx < 38);
                    }

                    public java.lang.Object nextElement() {
                        java.lang.Object key = table[idx];
                        do idx += 2; while (idx < 38 && table[idx] == null);
                        return key;
                    }
                };
    }

    public static long pluralEval(long n) {
        return ((n == 1) ? 0 : 1);
    }

    public java.util.ResourceBundle getParent() {
        return parent;
    }
}