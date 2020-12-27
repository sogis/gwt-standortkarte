package ch.so.agi.standortkarte;

import static elemental2.dom.DomGlobal.console;
import static org.jboss.elemento.Elements.*;
import static org.jboss.elemento.EventType.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.dominokit.domino.ui.style.Unit.px;

import org.dominokit.domino.ui.icons.Icon;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.style.ColorScheme;
import org.dominokit.domino.ui.themes.Theme;
import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.jboss.elemento.HtmlContentBuilder;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
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
import elemental2.core.JsNumber;
import elemental2.core.JsString;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.Headers;
import elemental2.dom.RequestInit;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import ol.Collection;
import ol.Coordinate;
import ol.Extent;
import ol.Feature;
import ol.FeatureOptions;
import ol.Map;
import ol.MapBrowserEvent;
import ol.MapOptions;
import ol.OLFactory;
import ol.Overlay;
import ol.OverlayOptions;
import ol.View;
import ol.ViewOptions;
import ol.control.Control;
import ol.event.EventListener;
import ol.format.GeoJson;
import ol.format.Wkt;
import ol.geom.Geometry;
import ol.geom.Point;
import ol.interaction.DefaultInteractionsOptions;
import ol.interaction.Interaction;
import ol.layer.Base;
import ol.layer.Image;
import ol.layer.LayerOptions;
import ol.layer.Tile;
import ol.layer.VectorLayerOptions;
import ol.proj.Projection;
import ol.proj.ProjectionOptions;
import ol.source.ImageWms;
import ol.source.ImageWmsOptions;
import ol.source.ImageWmsParams;
import ol.source.Vector;
import ol.source.VectorOptions;
import ol.source.Wmts;
import ol.source.WmtsOptions;
import ol.style.Circle;
import ol.style.CircleOptions;
import ol.style.IconOptions;
import ol.style.Stroke;
import ol.style.Style;
import ol.style.StyleOptions;
import ol.tilegrid.TileGrid;
import ol.tilegrid.WmtsTileGrid;
import ol.tilegrid.WmtsTileGridOptions;
import proj4.Proj4;

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
            DomGlobal.fetch(SEARCH_SERVICE_URL + egid.trim().toLowerCase(), requestInit).then(response -> {
                if (!response.ok) {
                    return null;
                }
                return response.text();
            }).then(json -> {
                JsPropertyMap<?> parsed = Js.cast(Global.JSON.parse(json));
                JsArray<?> results = Js.cast(parsed.get("results"));

                if (results.getLength() > 0) {
                    JsPropertyMap<?> resultObj = Js.cast(results.getAt(0));
                    JsPropertyMap properties = (JsPropertyMap) resultObj.get("properties");

                    String strname_deinr = ((JsString) properties.get("strname_deinr")).normalize();
                    int dplz4 = new Double(((JsNumber) properties.get("dplz4")).valueOf()).intValue();
                    String ggdename = ((JsString) properties.get("ggdename")).normalize();

                    double easting = new Double(((JsNumber) properties.get("dkode")).valueOf());
                    double northing = new Double(((JsNumber) properties.get("dkodn")).valueOf());

                    // add popup                    
                    HtmlContentBuilder popupBuilder = div().id("popup");
                    popupBuilder.add(span().innerHtml(SafeHtmlUtils.fromTrustedString(strname_deinr+"<br>"+dplz4+"&nbsp;"+ggdename)));
                    HTMLElement popupElement = popupBuilder.element();     

                    DivElement overlay = Js.cast(popupElement);
                    OverlayOptions overlayOptions = OLFactory.createOptions();
                    overlayOptions.setElement(overlay);
                    overlayOptions.setPosition(new Coordinate(easting, northing));
                    overlayOptions.setOffset(OLFactory.createPixel(-100, -120));
                    Overlay popup = new Overlay(overlayOptions);
                    map.addOverlay(popup);

                    // add marker
                    Point geom = new Point(new Coordinate(easting, northing));

                    FeatureOptions featureOptions = OLFactory.createOptions();
                    featureOptions.setGeometry(geom);

                    Feature feature = new Feature(featureOptions);
                    
                    IconOptions iconOptions = new IconOptions();
                    iconOptions.setSrc("icon.png");
                    double[] anchor = {0.5, 1};
                    iconOptions.setAnchor(anchor);
                    ol.style.Icon icon = new ol.style.Icon(iconOptions);
                    StyleOptions styleOptions = new StyleOptions();
                    styleOptions.setImage(icon);
                    Style style = new Style(styleOptions);
                    feature.setStyle(style);

                    Collection<Feature> lstFeatures = new Collection<Feature>();
                    lstFeatures.push(feature);

                    VectorOptions vectorSourceOptions = OLFactory.createOptions();
                    vectorSourceOptions.setFeatures(lstFeatures);
                    Vector vectorSource = new Vector(vectorSourceOptions);

                    VectorLayerOptions vectorLayerOptions = OLFactory.createOptions();
                    vectorLayerOptions.setSource(vectorSource);
                    ol.layer.Vector vectorLayer = new ol.layer.Vector(vectorLayerOptions);

                    map.addLayer(vectorLayer);

                    map.getView().setCenter(geom.getCoordinates());
                    map.getView().setZoom(12);
                }

                return null;
            }).catch_(error -> {
                console.log(error);
                return null;
            });
        }
        
        body().add(new SearchBox(map));
        
    }

    private static native void updateURLWithoutReloading(String newUrl) /*-{
        $wnd.history.pushState(newUrl, "", newUrl);
    }-*/;
}
