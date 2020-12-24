package ch.so.agi.standortkarte.server;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import ch.so.agi.standortkarte.shared.SettingsResponse;
import ch.so.agi.standortkarte.shared.SettingsService;

public class SettingsServiceImpl extends RemoteServiceServlet implements SettingsService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${app.myVar}")
    private String myVar;

    @Override
    public void init() throws ServletException {
         super.init();
         SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, getServletContext());
    }
    
    @Override
    public SettingsResponse settingsServer() throws IllegalArgumentException, IOException {
        HashMap<String,Object> settings = new HashMap<String,Object>();        
        settings.put("MY_VAR", myVar);
        SettingsResponse response = new SettingsResponse();
        response.setSettings(settings);
        return response;
    }
}
