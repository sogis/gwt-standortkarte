package ch.so.agi.standortkarte;

import static elemental2.dom.DomGlobal.console;
import static org.jboss.elemento.Elements.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.dominokit.domino.ui.cards.Card;
import org.dominokit.domino.ui.dropdown.DropDownMenu;
import org.dominokit.domino.ui.forms.Radio;
import org.dominokit.domino.ui.forms.RadioGroup;
import org.dominokit.domino.ui.forms.SuggestBox;
import org.dominokit.domino.ui.forms.SuggestBox.DropDownPositionDown;
import org.dominokit.domino.ui.forms.SuggestBoxStore;
import org.dominokit.domino.ui.forms.SuggestItem;
import org.dominokit.domino.ui.icons.Icon;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.icons.MdiIcon;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.utils.HasChangeHandlers.ChangeHandler;
import org.dominokit.domino.ui.utils.HasSelectionHandler.SelectionHandler;
import org.dominokit.domino.ui.utils.TextNode;
import org.gwtproject.event.shared.HandlerRegistration;
import org.jboss.elemento.Attachable;
import org.jboss.elemento.IsElement;

import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.core.JsNumber;
import elemental2.core.JsString;
import elemental2.core.JsBoolean;
import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventInit;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.Headers;
import elemental2.dom.KeyboardEvent;
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
        
        SuggestBoxStore dynamicStore = new SuggestBoxStore() {
            
            @Override
            public void filter(String value, SuggestionsHandler suggestionsHandler) {
                if (value.trim().length() == 0) {
                    return;
                }
                
                RequestInit requestInit = RequestInit.create();
                Headers headers = new Headers();
                headers.append("Content-Type", "application/x-www-form-urlencoded");
                requestInit.setHeaders(headers);
                
                DomGlobal.fetch(SEARCH_SERVICE_URL + value.trim().toLowerCase(), requestInit)
                .then(response -> {
                    if (!response.ok) {
                        return null;
                    }
                    return response.text();
                })
                .then(json -> {
                    List<SuggestItem<SearchResult>> suggestItems = new ArrayList<SuggestItem<SearchResult>>();

                    JsPropertyMap<?> parsed = Js.cast(Global.JSON.parse(json));
                    JsArray<?> results = Js.cast(parsed.get("results"));
                    for (int i = 0; i < results.length; i++) {
                        JsPropertyMap<?> resultObj = Js.cast(results.getAt(i));
                        if (resultObj.has("attrs")) {
                            JsPropertyMap attrs = (JsPropertyMap) resultObj.get("attrs");
                            String label = ((JsString) attrs.get("label")).normalize();
                            double lat = new Double(((JsNumber) attrs.get("lat")).valueOf()).doubleValue();
                            double lon = new Double(((JsNumber) attrs.get("lon")).valueOf()).doubleValue();
 
                            SearchResult searchResult = new SearchResult();
                            searchResult.setLabel(label.replace("<b>", "").replace("</b>", ""));
                            searchResult.setLat(lat);
                            searchResult.setLon(lon);

                            SuggestItem<SearchResult> suggestItem = SuggestItem.create(searchResult, searchResult.getLabel(), Icons.ALL.mail());
                            suggestItems.add(suggestItem);
                        }
                    }
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
        
        {
            suggestBoxStart = SuggestBox.create("Adresssuche: Start", dynamicStore);
            suggestBoxStart.setId("SuggestBoxStart");
            suggestBoxStart.addLeftAddOn(Icons.ALL.search());
            suggestBoxStart.setFocusColor(Color.RED);
            
            HTMLElement resetIcon = Icons.ALL.close().setId("SearchResetIcon").element();
            resetIcon.style.cursor = "pointer";
    
            resetIcon.addEventListener("click", new EventListener() {
                @Override
                public void handleEvent(Event evt) {
                    HTMLInputElement el =(HTMLInputElement) suggestBoxStart.getInputElement().element();
                    el.value = "";
                    suggestBoxStart.unfocus();

                    CustomEventInit eventInit = CustomEventInit.create();
                    eventInit.setBubbles(true);
                    CustomEvent customEvent = new CustomEvent("startingPointDeleted", eventInit);
                    root.dispatchEvent(customEvent);
                }
            });
            
            suggestBoxStart.addRightAddOn(resetIcon);
            
            suggestBoxStart.getInputElement().setAttribute("autocomplete", "off");
            suggestBoxStart.getInputElement().setAttribute("spellcheck", "false");
            DropDownMenu suggestionsMenu = suggestBoxStart.getSuggestionsMenu();
            suggestionsMenu.setPosition(new DropDownPositionDown());
            suggestionsMenu.setSearchable(false);
            
            suggestBoxStart.addSelectionHandler(new SelectionHandler() {
                @Override
                public void onSelection(Object value) {
                    SuggestItem<SearchResult> item = (SuggestItem<SearchResult>) value;
                    SearchResult result = (SearchResult) item.getValue();
                    HTMLInputElement el =(HTMLInputElement) suggestBoxStart.getInputElement().element();
                    el.value = result.getLabel();
                    
                    CustomEventInit eventInit = CustomEventInit.create();
                    eventInit.setDetail(result);
                    eventInit.setBubbles(true);
                    CustomEvent customEvent = new CustomEvent("startingPointChanged", eventInit);
                    root.dispatchEvent(customEvent);
                }
            });
        }
        
        {    
            suggestBoxFinish = SuggestBox.create("Adresssuche: Ziel", dynamicStore);
            suggestBoxFinish.setId("SuggestBoxFinish");
            suggestBoxFinish.addLeftAddOn(Icons.ALL.search());
            suggestBoxFinish.setFocusColor(Color.RED);
            
            HTMLElement resetIcon = Icons.ALL.close().setId("SearchResetIcon").element();
            resetIcon.style.cursor = "pointer";
    
            resetIcon.addEventListener("click", new EventListener() {
                @Override
                public void handleEvent(Event evt) {
                    HTMLInputElement el =(HTMLInputElement) suggestBoxFinish.getInputElement().element();
                    el.value = "";
                    suggestBoxFinish.unfocus();

                    CustomEventInit eventInit = CustomEventInit.create();
                    eventInit.setBubbles(true);
                    CustomEvent customEvent = new CustomEvent("finishingPointDeleted", eventInit);
                    root.dispatchEvent(customEvent);
                }
            });
            
            suggestBoxFinish.addRightAddOn(resetIcon);

    
            suggestBoxFinish.getInputElement().setAttribute("autocomplete", "off");
            suggestBoxFinish.getInputElement().setAttribute("spellcheck", "false");
            DropDownMenu suggestionsMenu = suggestBoxFinish.getSuggestionsMenu();
            suggestionsMenu.setPosition(new DropDownPositionDown());
            suggestionsMenu.setSearchable(false);

            suggestBoxFinish.addSelectionHandler(new SelectionHandler() {
                @Override
                public void onSelection(Object value) {
                    SuggestItem<SearchResult> item = (SuggestItem<SearchResult>) value;
                    SearchResult result = (SearchResult) item.getValue();
                    HTMLInputElement el =(HTMLInputElement) suggestBoxFinish.getInputElement().element();
                    el.value = result.getLabel();
                    
                    CustomEventInit eventInit = CustomEventInit.create();
                    eventInit.setDetail(result);
                    eventInit.setBubbles(true);
                    CustomEvent customEvent = new CustomEvent("finishingPointChanged", eventInit);
                    root.dispatchEvent(customEvent);
                }
            });
        }
                
        HTMLElement suggestBoxDiv = div().id("suggestBoxDiv").element();
        suggestBoxDiv.appendChild(suggestBoxStart.element());
        suggestBoxDiv.appendChild(suggestBoxFinish.element());
        
        RadioGroup radioGroup = RadioGroup.create("device", "")
        .appendChild(Radio.create("car", "Mit dem Auto").check())
        .appendChild(Radio.create("bike", "Mit dem Fahrrad"))
        .appendChild(Radio.create("foot", "Zu Fuss"))
        .horizontal();
        suggestBoxDiv.appendChild(radioGroup.element());
        
        radioGroup.addChangeHandler(new ChangeHandler() {
            @Override
            public void onValueChanged(Object value) {
                CustomEventInit eventInit = CustomEventInit.create();
                eventInit.setDetail(value);
                eventInit.setBubbles(true);
                CustomEvent customEvent = new CustomEvent("meansOfTransportChanged", eventInit);
                root.dispatchEvent(customEvent);                
            }
        });
        
        Card card = Card.create("Navigation","")
                .setId("searchCard")
                .setCollapsible()
                .setHeaderBackground(Color.GREY_LIGHTEN_5)
                .style().setColor("#333333").get()
                .appendChild(suggestBoxDiv);
        
        // Heuristisch (+/- iPhone SE/5)
        if (body().element().clientHeight < 600) {
            card.collapse();
        }
        
//        root.appendChild(suggestBoxDiv);
        root.appendChild(card.element());
        
//        console.log(body().element().clientHeight);
//        console.log(root.clientWidth);

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

    public SuggestBox getSuggestBoxStart() {
        return suggestBoxStart;
    }

    public SuggestBox getSuggestBoxFinish() {
        return suggestBoxFinish;
    }
}
