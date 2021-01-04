package ch.so.agi.standortkarte;

import static elemental2.dom.DomGlobal.console;
import static elemental2.dom.DomGlobal.fetch;
import static org.jboss.elemento.Elements.*;
import static org.dominokit.domino.ui.style.Unit.px;

import java.util.ArrayList;
import java.util.List;

import org.dominokit.domino.ui.animations.Animation;
import org.dominokit.domino.ui.animations.Transition;
import org.dominokit.domino.ui.button.Button;
import org.dominokit.domino.ui.button.ButtonSize;
import org.dominokit.domino.ui.icons.Icon;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.loaders.Loader;
import org.dominokit.domino.ui.loaders.LoaderEffect;
import org.dominokit.domino.ui.popover.PopupPosition;
import org.dominokit.domino.ui.popover.Tooltip;
import org.dominokit.domino.ui.style.ColorScheme;
import org.dominokit.domino.ui.style.StyleType;
import org.dominokit.domino.ui.themes.Theme;
import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.jboss.elemento.HtmlContentBuilder;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;

import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.core.JsNumber;
import elemental2.core.JsString;
import elemental2.dom.CustomEvent;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.Headers;
import elemental2.dom.RequestInit;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import ol.Collection;
import ol.Coordinate;
import ol.Extent;
import ol.Feature;
import ol.FeatureOptions;
import ol.Geolocation;
import ol.GeolocationOptions;
import ol.Map;
import ol.OLFactory;
import ol.Overlay;
import ol.OverlayOptions;
import ol.PositionOptions;
import ol.Size;
import ol.View;
import ol.ViewFitOptions;
import ol.geom.LineString;
import ol.geom.Point;
import ol.layer.Base;
import ol.layer.VectorLayerOptions;
import ol.proj.Projection;
import ol.proj.ProjectionOptions;
import ol.source.Vector;
import ol.source.VectorOptions;
import ol.style.Circle;
import ol.style.CircleOptions;
import ol.style.Fill;
import ol.style.IconOptions;
import ol.style.Stroke;
import ol.style.Style;
import ol.style.StyleOptions;
import proj4.Proj4;

public class App implements EntryPoint {
    private MyMessages messages = GWT.create(MyMessages.class);

    // Application settings
    private String myVar;
    private String SEARCH_SERVICE_URL = "https://api3.geo.admin.ch/rest/services/api/MapServer/find?layer=ch.bfs.gebaeude_wohnungs_register&searchField=egid&returnGeometry=true&geometryFormat=geojson&contains=false&sr=2056&searchText=";    
    //private String ROUTING_SERVICE_URL = "https://routing.openstreetmap.de/routed-bike/route/v1/driving/7.516431670901193,47.214321749999996;7.5427398,47.2051249?overview=full&geometries=geojson&steps=false";

    // Format settings
    private NumberFormat fmtDefault = NumberFormat.getDecimalFormat();
    private NumberFormat fmtPercent = NumberFormat.getFormat("#0.0");

    private static final String EPSG_2056 = "EPSG:2056";
    private static final String EPSG_4326 = "EPSG:4326"; 
    private Projection projection;

    private String ID_ATTR_NAME = "id";
    private String MAP_DIV_ID = "map";
    private String POPUP_LAYER_ID = "popup_layer";
    private String ROUTE_LAYER_ID = "route_layer";

    private Map map;
    private Overlay popup;
    private SearchBox searchBox;
    
    private Double lonStart;
    private Double latStart;
    private Double lonFinish;    
    private Double latFinish;
    
    private String meansOfTransportation = "car";
    
    private Animation posBtnAnimation;

    public void onModuleLoad() {
        // fetch settings form server
        // ...
        init();        
    }

    @SuppressWarnings("unchecked")
    private void init() {
        // make LV95 known to ol3
        Proj4.defs(EPSG_2056, "+proj=somerc +lat_0=46.95240555555556 +lon_0=7.439583333333333 +k_0=1 +x_0=2600000 +y_0=1200000 +ellps=bessel +towgs84=674.374,15.056,405.346,0,0,0,0 +units=m +no_defs");
        ol.proj.Proj4.register(Proj4.get());

        ProjectionOptions projectionOptions = OLFactory.createOptions();
        projectionOptions.setCode(EPSG_2056);
        projectionOptions.setUnits("m");
        projectionOptions.setExtent(new Extent(2420000, 1030000, 2900000, 1350000));
        projection = new Projection(projectionOptions);
        Projection.addProjection(projection);

        // change theme
        Theme theme = new Theme(ColorScheme.WHITE);
        theme.apply();
        
        // add map div for ol3 map
        HTMLElement mapElement = div().id(MAP_DIV_ID).element();
        body().add(mapElement);

        map = MapPresets.getColorMap(MAP_DIV_ID);

        // add searchbox
        searchBox = new SearchBox(map);
        
        body().element().addEventListener("startingPointChanged", new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                CustomEvent customEvent = (CustomEvent) evt;
                SearchResult result = (SearchResult) customEvent.detail;
                
                lonStart = result.getLon();
                latStart = result.getLat();
                
                calculateRoute();
            }
        });
        
        body().element().addEventListener("startingPointDeleted", new EventListener() {
            @Override
            public void handleEvent(Event evt) {                
                lonStart = null;
                latStart = null;
                
                removeRoute();
            }
        });
        
        body().element().addEventListener("finishingPointChanged", new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                CustomEvent customEvent = (CustomEvent) evt;
                SearchResult result = (SearchResult) customEvent.detail;
                
                lonFinish = result.getLon();
                latFinish = result.getLat();
                
                calculateRoute();
            }
        });
        
        body().element().addEventListener("finishingPointDeleted", new EventListener() {
            @Override
            public void handleEvent(Event evt) {                
                lonFinish = null;
                latFinish = null;
                
                removeRoute();
                removePopup();
            }
        });
        
        body().element().addEventListener("meansOfTransportChanged", new EventListener() {
            @Override
            public void handleEvent(Event evt) {   
                CustomEvent customEvent = (CustomEvent) evt;
                meansOfTransportation = (String) customEvent.detail;
                calculateRoute();
            }
        });
        
        body().add(searchBox);
        
        // add geolocation         
        ol.layer.Vector posVectorLayer = new ol.layer.Vector();
        map.addLayer(posVectorLayer);
        
        ol.source.Vector posVectorSource = new ol.source.Vector();
        posVectorLayer.setSource(posVectorSource);
        
        Feature positionFeature = new Feature();
        Feature accuracyFeature = new Feature();

        Stroke posStroke = new Stroke();
        posStroke.setWidth(2);
        posStroke.setColor(new ol.color.Color(255, 255, 255, 1));
        Fill posFill = new Fill();
        posFill.setColor(new ol.color.Color(51, 153, 204, 1));

        CircleOptions circleOptions = new CircleOptions();
        circleOptions.setRadius(6);
        circleOptions.setStroke(posStroke);
        circleOptions.setFill(posFill);

        StyleOptions posStyleOptions = new StyleOptions();
        posStyleOptions.setImage(new Circle(circleOptions));
        Style posStyle = new Style(posStyleOptions);

        positionFeature.setStyle(posStyle);

        posVectorSource.addFeature(positionFeature);
        posVectorSource.addFeature(accuracyFeature);
        
        PositionOptions positionOptions = new PositionOptions();
        positionOptions.setEnableHighAccuracy(true);

        GeolocationOptions geolocationOptions = new GeolocationOptions();
        geolocationOptions.setTrackingOptions(positionOptions);
        geolocationOptions.setProjection(map.getView().getProjection());

        Geolocation geolocation = new Geolocation(geolocationOptions);
        geolocation.addChangeListener((ol.events.Event event) -> {
            positionFeature.setGeometry(new Point(geolocation.getPosition()));
            map.getView().setCenter(geolocation.getPosition());
        });
        
        ViewFitOptions viewFitOptions = OLFactory.createOptions();
        viewFitOptions.setSize(new Size(100, 100));

        geolocation.on("change:accuracyGeometry", (ol.events.Event event) -> {
            posBtnAnimation.stop();
            
            accuracyFeature.setGeometry(geolocation.getAccuracyGeometry());
            map.getView().fit(geolocation.getAccuracyGeometry(), viewFitOptions);
        });
        
        geolocation.on("error", (ol.events.Event event) -> {
            posBtnAnimation.stop();
            
            Window.alert("Could't determine location!");
        });

        Button geolocationBtn = Button.create(Icons.ALL.gps_fixed()) // adjust()
                .circle()
                .setSize(ButtonSize.LARGE)
                .setButtonType(StyleType.DEFAULT)
                .style()
                .setColor("#333333")
                .setMargin(px.of(5)).get().setId("geolocation");
        
        geolocationBtn.addClickListener(new EventListener() {
            @Override
            public void handleEvent(Event evt) {                
                if (!geolocation.getTracking()) {
                    if (geolocation.getAccuracyGeometry() == null) {
                        posBtnAnimation = Animation.create(geolocationBtn.element()).transition(Transition.PULSE).duration(1000).infinite().animate();
                    }
                    
                    console.log(geolocation.getAccuracyGeometry());
                    
                    geolocationBtn.style().setColor("#F44336");
                    geolocation.setTracking(true);
                } else {
                    geolocationBtn.style().setColor("#333333");
                    geolocation.setTracking(false); // This will not remove the point.
                }
            }
        });
        
        body().add(geolocationBtn);

        // handle egrid from location parameter 
        if (Window.Location.getParameter("egid") != null) {
            String egid = Window.Location.getParameter("egid").toString();

            RequestInit requestInit = RequestInit.create();
            Headers headers = new Headers();
            headers.append("Content-Type", "application/x-www-form-urlencoded");
            requestInit.setHeaders(headers);

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
                    overlayOptions.setOffset(OLFactory.createPixel(-100, 10));
                    popup = new Overlay(overlayOptions);
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
                    vectorLayer.set(ID_ATTR_NAME, POPUP_LAYER_ID);

                    map.addLayer(vectorLayer);

                    // transform finishing point
                    Point transformedGeom = (Point) geom.clone().transform(Projection.get(EPSG_2056), Projection.get(EPSG_4326));
                    lonFinish = transformedGeom.getCoordinates().getX();
                    latFinish = transformedGeom.getCoordinates().getY();

                    // set finishing address in suggestbox
                    ((HTMLInputElement) searchBox.getSuggestBoxFinish().getInputElement().element()).value = strname_deinr + " " + + dplz4 + " " + ggdename;
                    searchBox.getSuggestBoxFinish().focus();
                    
                    // after all is done: center and zoom map view
                    map.getView().setCenter(geom.getCoordinates());
                    map.getView().setZoom(12);                    
                }

                return null;
            }).catch_(error -> {
                console.log(error);
                return null;
            });
        }
    }
    
    private void calculateRoute() {
        if (lonStart == null && latStart == null && lonFinish == null && latFinish == null) {
            return;
        }
                
        RequestInit requestInit = RequestInit.create();
        Headers headers = new Headers();
        headers.append("Content-Type", "application/x-www-form-urlencoded");
        requestInit.setHeaders(headers);

        //https://routing.osm.ch/routed-bike/route/v1/driving/
        //https://routing.osm.ch/routed-foot/route/v1/driving/7.516431670901193,47.214321749999996;7.54274,47.205125?overview=false&alternatives=true&steps=true&hints=ce4DgK6ECoAAAAAACwAAAAAAAAA8AQAAbgAAAAUAAAAAAAAAnAAAADcAAADARwMAyQYAACOxcgDDb9ACELFyAPJu0AIFABEPeO2YXw==;2bsKgP___39QbwAAQgAAAFQAAAAAAAAAAAAAAEIAAABUAAAAAAAAAAAAAACJhAcAyQYAAOsXcwCuStAC1BdzAAVL0AIAABEPeO2YXw==
        
        String ROUTING_SERVICE_URL = "https://routing.osm.ch/routed-"+ meansOfTransportation +"/route/v1/driving/";
        String query = ROUTING_SERVICE_URL + lonStart + "," +latStart + ";" + lonFinish + "," + latFinish;
        query += "?overview=full&geometries=geojson&steps=false";
        DomGlobal.fetch(query, requestInit).then(response -> {
            if (!response.ok) {
                return null;
            }
            return response.text();
        }).then(json -> {            
            JsPropertyMap<?> parsed = Js.cast(Global.JSON.parse(json));
            JsArray<?> routes = Js.cast(parsed.get("routes"));

            LineString route;
            if (routes.getLength() > 0) {
                JsPropertyMap<?> routeObj = Js.cast(routes.getAt(0));
                JsPropertyMap<?> geometry = (JsPropertyMap) routeObj.get("geometry");
                JsArray<?> coordinates = Js.cast(geometry.get("coordinates"));
                
                List<Coordinate> coords = new ArrayList<Coordinate>();
                for (int i=0; i<coordinates.getLength(); i++) {                    
                    JsArray<?> coordJsArray = Js.cast(coordinates.getAt(i));
                    double lon = new Double(((JsNumber) coordJsArray.getAt(0)).valueOf());
                    double lat = new Double(((JsNumber) coordJsArray.getAt(1)).valueOf());

                    Coordinate coordinate = new Coordinate(lon, lat);
                    coords.add(coordinate);
                }
                Coordinate[] coordsArray = new Coordinate[coords.size()];
                route = new LineString(coords.toArray(coordsArray));

                LineString routeTransformed = (LineString) route.clone().transform(Projection.get(EPSG_4326), Projection.get(EPSG_2056));
                
                if (getMapLayerById(ROUTE_LAYER_ID) != null) {
                    map.removeLayer(getMapLayerById(ROUTE_LAYER_ID));
                }
                
                FeatureOptions featureOptions = OLFactory.createOptions();
                featureOptions.setGeometry(routeTransformed);

                Feature feature = new Feature(featureOptions);

                Style style = new Style();
                Stroke stroke = new Stroke();
                stroke.setWidth(10);
                stroke.setColor(new ol.color.Color(230, 0, 0, 0.7));
                //stroke.setColor(new ol.color.Color(130, 145, 213, 1));
                style.setStroke(stroke);
                feature.setStyle(style);

                ol.Collection<Feature> lstFeatures = new ol.Collection<Feature>();
                lstFeatures.push(feature);

                VectorOptions vectorSourceOptions = OLFactory.createOptions();
                vectorSourceOptions.setFeatures(lstFeatures);
                Vector vectorSource = new Vector(vectorSourceOptions);

                VectorLayerOptions vectorLayerOptions = OLFactory.createOptions();
                vectorLayerOptions.setSource(vectorSource);
                ol.layer.Vector vectorLayer = new ol.layer.Vector(vectorLayerOptions);
                vectorLayer.set(ID_ATTR_NAME, ROUTE_LAYER_ID);

                map.addLayer(vectorLayer);
                
                Extent extent = routeTransformed.getExtent();
                View view = map.getView();
                double resolution = view.getResolutionForExtent(extent);
                view.setZoom(Math.floor(view.getZoomForResolution(resolution)) - 0);
                
                double x = extent.getLowerLeftX() + extent.getWidth() / 2;
                double y = extent.getLowerLeftY() + extent.getHeight() / 2;
                view.setCenter(new Coordinate(x, y));                    
            }

            return null;
        }).catch_(error -> {
            console.log(error);
            return null;
        });
    }
    
    private void removeRoute() {
        if (getMapLayerById(ROUTE_LAYER_ID) != null) {
            map.removeLayer(getMapLayerById(ROUTE_LAYER_ID));
        }
    }
    
    private void removePopup() {
        if (getMapLayerById(POPUP_LAYER_ID) != null) {
            map.removeLayer(getMapLayerById(POPUP_LAYER_ID));
            map.removeOverlay(popup);
        }
    }
    
    private Base getMapLayerById(String id) {
        ol.Collection<Base> layers = map.getLayers();
        for (int i = 0; i < layers.getLength(); i++) {
            Base item = layers.item(i);
            try {
                String layerId = item.get(ID_ATTR_NAME);
                if (layerId == null) {
                    continue;
                }
                if (layerId.equalsIgnoreCase(id)) {
                    return item;
                }
            } catch (Exception e) {
                console.log(e.getMessage());
                console.log("should not reach here");
            }
        }
        return null;
    }
    
    private static native void updateURLWithoutReloading(String newUrl) /*-{
        $wnd.history.pushState(newUrl, "", newUrl);
    }-*/;
}
