package com.kms.katalon.core.helper;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.internal.preferences.InstancePreferences;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.kms.katalon.core.util.ArrayUtil;

@SuppressWarnings("restriction")
public class WorkbenchPreferencesHelper {
    public static final String ECLIPSE_WORKBENCH_NODE_ID = "org.eclipse.ui.workbench";

    private static final String LOCATION_FIELD = "location";

    public static final InstancePreferences getPreferencesInstance() {
        InstancePreferences prefs = (InstancePreferences) InstanceScope.INSTANCE.getNode(ECLIPSE_WORKBENCH_NODE_ID);
        return prefs;
    }

    public static final File getPreferenceFile() {
        Field location;
        try {
            location = InstancePreferences.class.getDeclaredField(LOCATION_FIELD);
        } catch (NoSuchFieldException | SecurityException e1) {
            return null;
        }

        if (location == null) {
            return null;
        }

        location.setAccessible(true);
        Path prefsLocation;
        try {
            prefsLocation = (Path) location.get(getPreferencesInstance());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return null;
        }

        return prefsLocation != null ? new File(prefsLocation.toString()) : null;
    }

    public static Document getPreferenceSection(String name) {
        Document doc = XMLHelper.readXML(getPreferencesInstance().get(name, StringUtils.EMPTY));
        return doc;
    }

    public static void setPreferencefSection(String name, Document xmlDoc) {
        String xmlString = XMLHelper.docToString(xmlDoc);
        setPreferenceSection(name, xmlString);
    }

    public static void setPreferenceSection(String name, String value) {
        getPreferencesInstance().put(name, value);
    }

    public static Node findNodeByAttributes(String parentSectionName, String... attributes) {
        return findNodeByAttributes(parentSectionName, ArrayUtil.toMap(attributes));
    }

    public static Node findNodeByAttributes(String parentSectionName, Map<String, String> nodeAttributes) {
        Document section = getPreferenceSection(parentSectionName);
        return XMLHelper.findNodeByAttributes(section, nodeAttributes);
    }
}
