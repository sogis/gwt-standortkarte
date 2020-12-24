package ch.so.agi.standortkarte.shared;

import java.util.HashMap;
import com.google.gwt.user.client.rpc.IsSerializable;

public class SettingsResponse implements IsSerializable {
    private HashMap<String,Object> settings;

    public HashMap<String, Object> getSettings() {
        return settings;
    }

    public void setSettings(HashMap<String, Object> settings) {
        this.settings = settings;
    } 
}
