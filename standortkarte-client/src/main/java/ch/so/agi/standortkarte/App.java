package ch.so.agi.standortkarte;

import static elemental2.dom.DomGlobal.console;
import static org.jboss.elemento.Elements.*;

import java.util.List;
import java.util.stream.Stream;

import static org.dominokit.domino.ui.style.Unit.px;

import org.dominokit.domino.ui.style.ColorScheme;
import org.dominokit.domino.ui.themes.Theme;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.dom.DomGlobal;
import elemental2.dom.Headers;
import elemental2.dom.RequestInit;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import ol.Map;

public class App implements EntryPoint {
    private MyMessages messages = GWT.create(MyMessages.class);
    
    // Application settings
    private String myVar;
    
    // Format settings
    private NumberFormat fmtDefault = NumberFormat.getDecimalFormat();
    private NumberFormat fmtPercent = NumberFormat.getFormat("#0.0");
        
    private String MAP_DIV_ID = "map";

    public void onModuleLoad() {
        // fetch settings form server
        // ...
        init();
    }

    @SuppressWarnings("unchecked")
    private void init() {         
        Theme theme = new Theme(ColorScheme.WHITE);
        theme.apply();

        body().add(div().id(MAP_DIV_ID));
        
        Map map = MapPresets.getColorMap(MAP_DIV_ID);
        
        
        if (Window.Location.getParameter("egid") != null) {
            String egid = Window.Location.getParameter("egid").toString();
            
            RequestInit requestInit = RequestInit.create();
            Headers headers = new Headers();
            headers.append("Content-Type", "application/x-www-form-urlencoded"); 
            requestInit.setHeaders(headers);

            String SEARCH_SERVICE_URL = "https://api3.geo.admin.ch/rest/services/api/MapServer/find?layer=ch.bfs.gebaeude_wohnungs_register&searchField=egid&returnGeometry=true&geometryFormat=geojson&contains=false&sr=2056&searchText=";
            DomGlobal.fetch(SEARCH_SERVICE_URL + egid.trim().toLowerCase(), requestInit)
            .then(response -> {
                if (!response.ok) {
                    return null;
                }
                return response.text();
            })
            .then(json -> {
                
                Message msg = Js.cast(Global.JSON.parse(json));
                Stream.of(msg.results).forEach(r -> DomGlobal.console.log(r.featureId));

//                JsPropertyMap<?> parsed = Js.cast(Global.JSON.parse(json));
//                JsArray<?> results = Js.cast(parsed.get("results"));
//                
//                console.log(results);

                
//                List<SuggestItem<SearchResult>> suggestItems = new ArrayList<>();
//                JsPropertyMap<?> parsed = Js.cast(Global.JSON.parse(json));
//                JsArray<?> results = Js.cast(parsed.get("results"));
//                for (int i = 0; i < results.length; i++) {
//                    JsPropertyMap<?> resultObj = Js.cast(results.getAt(i));
//                    if (resultObj.has("feature")) {
//                        JsPropertyMap feature = (JsPropertyMap) resultObj.get("feature");
//                        String display = ((JsString) feature.get("display")).normalize();
//                        String dataproductId = ((JsString) feature.get("dataproduct_id")).normalize();
//                        String idFieldName = ((JsString) feature.get("id_field_name")).normalize();
//                        int featureId = new Double(((JsNumber) feature.get("feature_id")).valueOf()).intValue();
//                        List<Double> bbox = ((JsArray) feature.get("bbox")).asList();
//
//                        SearchResult searchResult = new SearchResult();
//                        searchResult.setLabel(display);
//                        searchResult.setDataproductId(dataproductId);
//                        searchResult.setIdFieldName(idFieldName);
//                        searchResult.setFeatureId(featureId);
//                        searchResult.setBbox(bbox);
//                        searchResult.setType("feature");
//                        
//                        Icon icon;
//                        if (dataproductId.contains("gebaeudeadressen")) {
//                            icon = Icons.ALL.mail();
//                        } else if (dataproductId.contains("grundstueck")) {
//                            icon = Icons.ALL.home();
//                        } else {
//                            icon = Icons.ALL.place();
//                        }
//                        
//                        SuggestItem<SearchResult> suggestItem = SuggestItem.create(searchResult, searchResult.getLabel(), icon);
//                        suggestItems.add(suggestItem);
//                    }
//                }
//                suggestionsHandler.onSuggestionsReady(suggestItems);
                return null;
            }).catch_(error -> {
                console.log(error);
                return null;
            });
        }
    }
    
    private static native void updateURLWithoutReloading(String newUrl) /*-{
        $wnd.history.pushState(newUrl, "", newUrl);
    }-*/;
    
    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    static class Message {
        public Result[] results;
    }
    
    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    static class Result {
        public String featureId;
    }
}
