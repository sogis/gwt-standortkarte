package ch.so.agi.standortkarte;

import static elemental2.dom.DomGlobal.console;
import static org.jboss.elemento.Elements.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.dominokit.domino.ui.dropdown.DropDownMenu;
import org.dominokit.domino.ui.forms.SuggestBox;
import org.dominokit.domino.ui.forms.SuggestBox.DropDownPositionDown;
import org.dominokit.domino.ui.forms.SuggestBoxStore;
import org.dominokit.domino.ui.forms.SuggestItem;
import org.dominokit.domino.ui.icons.Icon;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.icons.MdiIcon;
import org.dominokit.domino.ui.style.Color;
import org.gwtproject.event.shared.HandlerRegistration;
import org.jboss.elemento.Attachable;
import org.jboss.elemento.IsElement;

import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.core.JsNumber;
import elemental2.core.JsString;
import elemental2.core.JsBoolean;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventInit;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.Headers;
import elemental2.dom.MutationRecord;
import elemental2.dom.RequestInit;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import ol.Map;

public class SearchBox implements IsElement<HTMLElement>, Attachable {
    private String SEARCH_SERVICE_URL = "https://api3.geo.admin.ch/rest/services/api/SearchServer?sr=2056&limit=15&type=locations&origins=address&searchText=";

    private final HTMLElement root;
    private HTMLElement layerPanelContainer;
    private HandlerRegistration handlerRegistration;
    private Map map;
    private SuggestBox suggestBoxStart;
    private SuggestBox suggestBoxFinish;

    @SuppressWarnings("unchecked")
    public SearchBox(Map map) {
        this.map = map;
        root = div().id("searchBox").element();
        
        {
            SuggestBoxStore dynamicStore = new SuggestBoxStore() {
                
                @Override
                public void filter(String value, SuggestionsHandler suggestionsHandler) {
                    if (value.trim().length() == 0) {
                        return;
                    }
                    
                    RequestInit requestInit = RequestInit.create();
                    Headers headers = new Headers();
                    headers.append("Content-Type", "application/x-www-form-urlencoded"); // CORS and preflight...
                    requestInit.setHeaders(headers);
                    
                    console.log(SEARCH_SERVICE_URL + value.trim().toLowerCase());
    
                    DomGlobal.fetch(SEARCH_SERVICE_URL + value.trim().toLowerCase(), requestInit)
                    .then(response -> {
                        if (!response.ok) {
                            return null;
                        }
                        return response.text();
                    })
                    .then(json -> {
                        List<SuggestItem<SearchResult>> featureResults = new ArrayList<SuggestItem<SearchResult>>();
                        List<SuggestItem<SearchResult>> dataproductResults = new ArrayList<SuggestItem<SearchResult>>();
    
                        List<SuggestItem<SearchResult>> suggestItems = new ArrayList<>();
                        JsPropertyMap<?> parsed = Js.cast(Global.JSON.parse(json));
                        JsArray<?> results = Js.cast(parsed.get("results"));
                        for (int i = 0; i < results.length; i++) {
                            JsPropertyMap<?> resultObj = Js.cast(results.getAt(i));
                            if (resultObj.has("feature")) {
                                JsPropertyMap feature = (JsPropertyMap) resultObj.get("feature");
                                String display = ((JsString) feature.get("display")).normalize();
                                String dataproductId = ((JsString) feature.get("dataproduct_id")).normalize();
                                String idFieldName = ((JsString) feature.get("id_field_name")).normalize();
                                int featureId = new Double(((JsNumber) feature.get("feature_id")).valueOf()).intValue();
                                List<Double> bbox = ((JsArray) feature.get("bbox")).asList();
     
                                SearchResult searchResult = new SearchResult();
                                searchResult.setLabel(display);
                                searchResult.setDataproductId(dataproductId);
                                searchResult.setIdFieldName(idFieldName);
                                searchResult.setFeatureId(featureId);
                                searchResult.setBbox(bbox);
                                searchResult.setType("feature");
                                
                                Icon icon;
                                if (dataproductId.contains("gebaeudeadressen")) {
                                    icon = Icons.ALL.mail();
                                } else if (dataproductId.contains("grundstueck")) {
                                    icon = Icons.ALL.home();
                                } else if (dataproductId.contains("flurname"))  {
                                    icon = Icons.ALL.terrain();
                                } else {
                                    icon = Icons.ALL.place();
                                }
                                
                                SuggestItem<SearchResult> suggestItem = SuggestItem.create(searchResult, searchResult.getLabel(), icon);
                                featureResults.add(suggestItem);
    //                            suggestItems.add(suggestItem);                            
                                
                            } else if (resultObj.has("dataproduct")) {
                                JsPropertyMap dataproduct = (JsPropertyMap) resultObj.get("dataproduct");
                                String display = ((JsString) dataproduct.get("display")).normalize();
                                String dataproductId = ((JsString) dataproduct.get("dataproduct_id")).normalize();
    
                                SearchResult searchResult = new SearchResult();
                                searchResult.setLabel(display);
                                searchResult.setDataproductId(dataproductId);
                                searchResult.setType("dataproduct");
    
                                MdiIcon icon;
                                if (dataproduct.has("sublayers")) {
                                    icon = Icons.ALL.layers_plus_mdi();  
                                } else {
                                    icon = Icons.ALL.layers_mdi();
                                } 
                                
                                SuggestItem<SearchResult> suggestItem = SuggestItem.create(searchResult, searchResult.getLabel(), icon);                            
                                dataproductResults.add(suggestItem);
    //                            suggestItems.add(suggestItem);
                            }
                        }
    //                    SearchResult featureGroup = new SearchResult();
    //                    featureGroup.setLabel("<b>Orte</b>");
    //                    SuggestItem<SearchResult> featureGroupItem = SuggestItem.create(featureGroup, featureGroup.getLabel(), null);                            
    //                    suggestItems.add(featureGroupItem);
                        
                        suggestItems.addAll(featureResults);
                        suggestItems.addAll(dataproductResults);
    
                        suggestionsHandler.onSuggestionsReady(suggestItems);
                        return null;
                    }).catch_(error -> {
                        console.log(error);
                        return null;
                    });
                }
    
                @Override
                public void find(Object searchValue, Consumer handler) {
                    if (searchValue == null) {
                        return;
                    }
                    SearchResult searchResult = (SearchResult) searchValue;
                    SuggestItem<SearchResult> suggestItem = SuggestItem.create(searchResult, null);
                    handler.accept(suggestItem);
                }
            };
    
            suggestBoxStart = SuggestBox.create("Adresssuche: Start", dynamicStore);
            suggestBoxStart.setId("SuggestBoxStart");
            suggestBoxStart.addLeftAddOn(Icons.ALL.search());
            suggestBoxStart.setAutoSelect(false);
            suggestBoxStart.setFocusColor(Color.RED);
            suggestBoxStart.setFocusOnClose(false);
            
            HTMLElement resetIcon = Icons.ALL.close().setId("SearchResetIcon").element();
            resetIcon.style.cursor = "pointer";
    
            resetIcon.addEventListener("click", new EventListener() {
                @Override
                public void handleEvent(Event evt) {
                    HTMLInputElement el =(HTMLInputElement) suggestBoxStart.getInputElement().element();
                    el.value = "";
                    suggestBoxStart.unfocus();
    //                ol.source.Vector vectorSource = map.getHighlightLayer().getSource();
    //                vectorSource.clear(false); 
                }
            });
            
            suggestBoxStart.addRightAddOn(resetIcon);
            
            suggestBoxStart.getInputElement().addEventListener("focus", new EventListener() {
                @Override
                public void handleEvent(Event evt) {
    //                ol.source.Vector vectorSource = map.getHighlightLayer().getSource();
    //                vectorSource.clear(false); 
                }
            });
            
            // TODO open suggestionsMenu when clicking in suggestBox and some
            // text is in there.
    //        suggestBox.getInputElement().addClickListener(new EventListener() {
    //            @Override
    //            public void handleEvent(Event evt) {
    //                console.log("click");
    //                KeyboardEvent event = new KeyboardEvent("keydown");
    //                suggestBox.element().dispatchEvent(event);
    //            }
    //        });
    
            suggestBoxStart.getInputElement().setAttribute("autocomplete", "off");
            suggestBoxStart.getInputElement().setAttribute("spellcheck", "false");
            DropDownMenu suggestionsMenu = suggestBoxStart.getSuggestionsMenu();
            suggestionsMenu.setPosition(new DropDownPositionDown());
            suggestionsMenu.setSearchable(false);
        }
        
        {
            SuggestBoxStore dynamicStore = new SuggestBoxStore() {
                
                @Override
                public void filter(String value, SuggestionsHandler suggestionsHandler) {
                    if (value.trim().length() == 0) {
                        return;
                    }
                    
                    RequestInit requestInit = RequestInit.create();
                    Headers headers = new Headers();
                    headers.append("Content-Type", "application/x-www-form-urlencoded"); // CORS and preflight...
                    requestInit.setHeaders(headers);
                    
                    console.log(SEARCH_SERVICE_URL + value.trim().toLowerCase());
    
                    DomGlobal.fetch(SEARCH_SERVICE_URL + value.trim().toLowerCase(), requestInit)
                    .then(response -> {
                        if (!response.ok) {
                            return null;
                        }
                        return response.text();
                    })
                    .then(json -> {
                        List<SuggestItem<SearchResult>> featureResults = new ArrayList<SuggestItem<SearchResult>>();
                        List<SuggestItem<SearchResult>> dataproductResults = new ArrayList<SuggestItem<SearchResult>>();
    
                        List<SuggestItem<SearchResult>> suggestItems = new ArrayList<>();
                        JsPropertyMap<?> parsed = Js.cast(Global.JSON.parse(json));
                        JsArray<?> results = Js.cast(parsed.get("results"));
                        for (int i = 0; i < results.length; i++) {
                            JsPropertyMap<?> resultObj = Js.cast(results.getAt(i));
                            if (resultObj.has("feature")) {
                                JsPropertyMap feature = (JsPropertyMap) resultObj.get("feature");
                                String display = ((JsString) feature.get("display")).normalize();
                                String dataproductId = ((JsString) feature.get("dataproduct_id")).normalize();
                                String idFieldName = ((JsString) feature.get("id_field_name")).normalize();
                                int featureId = new Double(((JsNumber) feature.get("feature_id")).valueOf()).intValue();
                                List<Double> bbox = ((JsArray) feature.get("bbox")).asList();
     
                                SearchResult searchResult = new SearchResult();
                                searchResult.setLabel(display);
                                searchResult.setDataproductId(dataproductId);
                                searchResult.setIdFieldName(idFieldName);
                                searchResult.setFeatureId(featureId);
                                searchResult.setBbox(bbox);
                                searchResult.setType("feature");
                                
                                Icon icon;
                                if (dataproductId.contains("gebaeudeadressen")) {
                                    icon = Icons.ALL.mail();
                                } else if (dataproductId.contains("grundstueck")) {
                                    icon = Icons.ALL.home();
                                } else if (dataproductId.contains("flurname"))  {
                                    icon = Icons.ALL.terrain();
                                } else {
                                    icon = Icons.ALL.place();
                                }
                                
                                SuggestItem<SearchResult> suggestItem = SuggestItem.create(searchResult, searchResult.getLabel(), icon);
                                featureResults.add(suggestItem);
    //                            suggestItems.add(suggestItem);                            
                                
                            } else if (resultObj.has("dataproduct")) {
                                JsPropertyMap dataproduct = (JsPropertyMap) resultObj.get("dataproduct");
                                String display = ((JsString) dataproduct.get("display")).normalize();
                                String dataproductId = ((JsString) dataproduct.get("dataproduct_id")).normalize();
    
                                SearchResult searchResult = new SearchResult();
                                searchResult.setLabel(display);
                                searchResult.setDataproductId(dataproductId);
                                searchResult.setType("dataproduct");
    
                                MdiIcon icon;
                                if (dataproduct.has("sublayers")) {
                                    icon = Icons.ALL.layers_plus_mdi();  
                                } else {
                                    icon = Icons.ALL.layers_mdi();
                                } 
                                
                                SuggestItem<SearchResult> suggestItem = SuggestItem.create(searchResult, searchResult.getLabel(), icon);                            
                                dataproductResults.add(suggestItem);
    //                            suggestItems.add(suggestItem);
                            }
                        }
    //                    SearchResult featureGroup = new SearchResult();
    //                    featureGroup.setLabel("<b>Orte</b>");
    //                    SuggestItem<SearchResult> featureGroupItem = SuggestItem.create(featureGroup, featureGroup.getLabel(), null);                            
    //                    suggestItems.add(featureGroupItem);
                        
                        suggestItems.addAll(featureResults);
                        suggestItems.addAll(dataproductResults);
    
                        suggestionsHandler.onSuggestionsReady(suggestItems);
                        return null;
                    }).catch_(error -> {
                        console.log(error);
                        return null;
                    });
                }
    
                @Override
                public void find(Object searchValue, Consumer handler) {
                    if (searchValue == null) {
                        return;
                    }
                    SearchResult searchResult = (SearchResult) searchValue;
                    SuggestItem<SearchResult> suggestItem = SuggestItem.create(searchResult, null);
                    handler.accept(suggestItem);
                }
            };
    
            suggestBoxFinish = SuggestBox.create("Adresssuche: Ziel", dynamicStore);
            suggestBoxFinish.setId("SuggestBoxFinish");
            suggestBoxFinish.addLeftAddOn(Icons.ALL.search());
            suggestBoxFinish.setAutoSelect(false);
            suggestBoxFinish.setFocusColor(Color.RED);
            suggestBoxFinish.setFocusOnClose(false);
            
            HTMLElement resetIcon = Icons.ALL.close().setId("SearchResetIcon").element();
            resetIcon.style.cursor = "pointer";
    
            resetIcon.addEventListener("click", new EventListener() {
                @Override
                public void handleEvent(Event evt) {
                    HTMLInputElement el =(HTMLInputElement) suggestBoxFinish.getInputElement().element();
                    el.value = "";
                    suggestBoxFinish.unfocus();
    //                ol.source.Vector vectorSource = map.getHighlightLayer().getSource();
    //                vectorSource.clear(false); 
                }
            });
            
            suggestBoxFinish.addRightAddOn(resetIcon);
            
            suggestBoxFinish.getInputElement().addEventListener("focus", new EventListener() {
                @Override
                public void handleEvent(Event evt) {
    //                ol.source.Vector vectorSource = map.getHighlightLayer().getSource();
    //                vectorSource.clear(false); 
                }
            });
            
            // TODO open suggestionsMenu when clicking in suggestBox and some
            // text is in there.
    //        suggestBox.getInputElement().addClickListener(new EventListener() {
    //            @Override
    //            public void handleEvent(Event evt) {
    //                console.log("click");
    //                KeyboardEvent event = new KeyboardEvent("keydown");
    //                suggestBox.element().dispatchEvent(event);
    //            }
    //        });
    
            suggestBoxFinish.getInputElement().setAttribute("autocomplete", "off");
            suggestBoxFinish.getInputElement().setAttribute("spellcheck", "false");
            DropDownMenu suggestionsMenu = suggestBoxFinish.getSuggestionsMenu();
            suggestionsMenu.setPosition(new DropDownPositionDown());
            suggestionsMenu.setSearchable(false);

        }
        
        
        // todo
        
        
        
        HTMLElement suggestBoxDiv = div().id("suggestBoxDiv").element();
        suggestBoxDiv.appendChild(suggestBoxStart.element());
        suggestBoxDiv.appendChild(suggestBoxFinish.element());
        root.appendChild(suggestBoxDiv);


    }        
    
    @Override
    public void attach(MutationRecord mutationRecord) {}
    
    @Override
    public void detach(MutationRecord mutationRecord) {
        if (handlerRegistration != null) {
            handlerRegistration.removeHandler();
        }
    }

    @Override
    public HTMLElement element() {
        return root;
    }

}
