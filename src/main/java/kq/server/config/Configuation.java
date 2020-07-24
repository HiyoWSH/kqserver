package kq.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="kqserver",ignoreInvalidFields=true, ignoreUnknownFields=true)
public class Configuation {
    public static String runMode;
    public static long me;
    public static String mes;
    public static String miraiserver;
    public static String authKey;
    public static String groovyPath;

    public static String getRunMode() {
        return runMode;
    }

    public static void setRunMode(String runMode) {
        Configuation.runMode = runMode;
    }

    public static void setMe(long me) {
        Configuation.me = me;
    }

    public static void setMes(String mes) {
        Configuation.mes = mes;
    }

    public static void setMiraiserver(String miraiserver) {
        Configuation.miraiserver = miraiserver;
    }

    public static void setAuthKey(String authKey) {
        Configuation.authKey = authKey;
    }

    public static long getMe() {
        return me;
    }

    public static String getMes() {
        return mes;
    }

    public static String getMiraiserver() {
        return miraiserver;
    }

    public static String getAuthKey() {
        return authKey;
    }

    public static String getGroovyPath() {
        return groovyPath;
    }

    public static void setGroovyPath(String groovyPath) {
        Configuation.groovyPath = groovyPath;
    }
}
