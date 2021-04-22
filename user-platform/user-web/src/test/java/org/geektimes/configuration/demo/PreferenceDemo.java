package org.geektimes.configuration.demo;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class PreferenceDemo {
    public static void main(String[] args) throws BackingStoreException {
        Preferences preferences = Preferences.userRoot();
        preferences.put("mykey","myvalue");
        preferences.flush();
        //preferences.remove("mykey");
        System.out.println(preferences.get("mykey", ""));
        System.out.println("绝对路径："+preferences.absolutePath());

        //Preferences preferences1 = Preferences.systemRoot();
    }
}
