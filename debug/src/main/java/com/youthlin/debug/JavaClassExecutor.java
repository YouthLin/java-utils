package com.youthlin.debug;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 创建: youthlin.chen
 * 时间: 2018-03-11 23:05
 */
public class JavaClassExecutor {
    private static final String PATH_SEPARATOR = System.getProperty("path.separator");
    private static final HotSwapClassloader CLASSLOADER = new HotSwapClassloader();

    public static Class hackSout(byte[] classByte) {
        try {
            ClassModifier cm = new ClassModifier(classByte);
            byte[] modifiedBytes = cm.modifyUTF8Constant(System.class.getName().replace(".", "/"),
                    HackSystem.class.getName().replace(".", "/"));
            return CLASSLOADER.loadByte(modifiedBytes);
        } catch (Throwable t) {
            throw new HackException(t);
        }
    }

    public static String execute(byte[] classByte) {
        HackSystem.clearBuffer();
        Class clazz = hackSout(classByte);
        try {
            @SuppressWarnings("unchecked")
            Method method = clazz.getMethod("main", String[].class);
            method.invoke(null, (Object) null);
        } catch (Throwable e) {
            e.printStackTrace(HackSystem.out);
            throw new HackException(e);
        }
        return HackSystem.getOutput();
    }

    //region getClasspath
    public static String getClasspath() {
        return getClasspath(JavaClassExecutor.class);
    }

    public static String getClasspath(Class<?> clazz) {
        Set<String> classpathSet = getClasspathSet(clazz);
        StringBuilder sb = new StringBuilder();
        for (String classpath : classpathSet) {
            if (sb.length() > 0) {
                sb.append(getPathSeparator());
            }
            sb.append(classpath);
        }
        return sb.toString();
    }

    public static Set<String> getClasspathSet() {
        return getClasspathSet(JavaClassExecutor.class);
    }

    public static Set<String> getClasspathSet(Class<?> clazz) {
        String jarPath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        jarPath = jarPath.substring(0, jarPath.lastIndexOf("/"));
        File dir = new File(jarPath);
        if (!dir.exists()) {
            return Collections.emptySet();
        }
        Set<String> result = new HashSet<String>();
        listFile(result, dir);
        return result;
    }

    private static void listFile(Set<String> result, File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) return;
            for (File f : files) {
                listFile(result, f);
            }
        } else {
            result.add(file.getAbsolutePath());
        }
    }
    //endregion

    public static String getPathSeparator() {
        return PATH_SEPARATOR;
    }

    public static void main(String[] args) {
        System.out.println(System.class.getName().replace(".", "/"));
    }

}
