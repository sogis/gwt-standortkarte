package ch.so.agi.standortkarte.shared;

import java.io.IOException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("settings")
public interface SettingsService extends RemoteService {
    SettingsResponse settingsServer() throws IllegalArgumentException, IOException;
}
