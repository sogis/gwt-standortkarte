package ch.so.agi.standortkarte.client;

import ol.Collection;
import ol.Coordinate;
import ol.Extent;
import ol.Map;
import ol.MapOptions;
import ol.OLFactory;
import ol.View;
import ol.ViewOptions;
import ol.control.Control;
import ol.interaction.DefaultInteractionsOptions;
import ol.interaction.Interaction;
import ol.layer.Base;
import ol.layer.Group;
import ol.layer.Image;
import ol.layer.LayerOptions;
import ol.layer.Tile;
import ol.proj.Projection;
import ol.proj.ProjectionOptions;
import ol.source.ImageWms;
import ol.source.ImageWmsOptions;
import ol.source.ImageWmsParams;
import ol.source.TileWms;
import ol.source.TileWmsOptions;
import ol.source.TileWmsParams;
import ol.source.Wmts;
import ol.source.WmtsOptions;
import ol.tilegrid.TileGrid;
import ol.tilegrid.WmtsTileGrid;
import ol.tilegrid.WmtsTileGridOptions;
import proj4.Proj4;

public class MapPresets {
    
    private MapPresets() {
        throw new AssertionError();
    }

    public static double resolutionsSogis[] = new double[] { 4000.0, 2000.0, 1000.0, 500.0, 250.0, 100.0, 50.0, 20.0, 10.0, 5.0, 2.5, 1.0, 0.5, 0.25, 0.1 };
    public static double resolutionsBgdi[] = new double[] { 4000.0, 3750.0, 3500.0, 3250.0, 3000.0, 2750.0, 2500.0, 2250.0, 2000.0, 1750.0, 1500.0, 1250.0, 1000.0, 750.0, 650.0, 500.0, 250.0, 100.0, 50.0, 20.0, 10.0, 5.0, 2.5, 2.0, 1.5, 1.0, 0.5, 0.25, 0.1 };

    public static Map getCadastralSurveyingWms(String mapId) {
        Proj4.defs("EPSG:2056", "+proj=somerc +lat_0=46.95240555555556 +lon_0=7.439583333333333 +k_0=1 +x_0=2600000 +y_0=1200000 +ellps=bessel +towgs84=674.374,15.056,405.346,0,0,0,0 +units=m +no_defs");

        ProjectionOptions projectionOptions = OLFactory.createOptions();
        projectionOptions.setCode("EPSG:2056");
        projectionOptions.setUnits("m");
        projectionOptions.setExtent(new Extent(2420000, 1030000, 2900000, 1350000));
        
        Projection projection = new Projection(projectionOptions);
        
        // geodienste.ch WMS tiled
        /*
        ol.layer.Tile geodiensteWmsLayer;
        {
            TileWmsParams imageWMSParams = OLFactory.createOptions();
            imageWMSParams.setLayers("LCSF,LCSFPROJ,LCOBJ,SOSF,SOOBJ,SOLI,SOPT,Liegenschaften,Gebaeudeadressen,Nomenklatur,Rohrleitungen,Hoheitsgrenzen,Fixpunkte");
            //imageWMSParams.set("FORMAT", "image/png; mode=8bit");
            imageWMSParams.set("FORMAT", "image/jpeg");
            imageWMSParams.set("TRANSPARENT", "false");
            imageWMSParams.set("TILED", "true");
    
            TileWmsOptions imageWMSOptions = OLFactory.createOptions();
            imageWMSOptions.setUrl("https://wfs.geodienste.ch/av/deu");
            imageWMSOptions.setParams(imageWMSParams);
    
            TileWms imageWMSSource = new TileWms(imageWMSOptions);
    
            LayerOptions layerOptions = OLFactory.createOptions();
            layerOptions.setSource(imageWMSSource);
    
            geodiensteWmsLayer = new ol.layer.Tile(layerOptions);
        }
        */
        // geodienste.ch WMS single
        ol.layer.Image geodiensteWmsLayer;        
        {
            ImageWmsParams imageWMSParams = OLFactory.createOptions();
            imageWMSParams.setLayers("LCSF,LCSFPROJ,LCOBJ,SOSF,SOOBJ,SOLI,SOPT,Liegenschaften,Gebaeudeadressen,Nomenklatur,Rohrleitungen,Hoheitsgrenzen,Fixpunkte");
            imageWMSParams.set("FORMAT", "image/jpeg");
            imageWMSParams.set("TRANSPARENT", "false");

            ImageWmsOptions imageWMSOptions = OLFactory.createOptions();
            imageWMSOptions.setUrl("https://wfs.geodienste.ch/av/deu");
            imageWMSOptions.setRatio(1.2f);
            imageWMSOptions.setParams(imageWMSParams);

            ImageWms imageWMSSource = new ImageWms(imageWMSOptions);

            LayerOptions layerOptions = OLFactory.createOptions();
            layerOptions.setSource(imageWMSSource);
            
            geodiensteWmsLayer = new Image(layerOptions);
        }
        // SO!GIS WMTS
        Tile sogisWmtsLayer;
        {
            WmtsOptions wmtsOptions = OLFactory.createOptions();
            wmtsOptions.setUrl("https://geo.so.ch/api/wmts/1.0.0/{Layer}/default/2056/{TileMatrix}/{TileRow}/{TileCol}");
            wmtsOptions.setLayer("ch.so.agi.hintergrundkarte_sw");
            wmtsOptions.setRequestEncoding("REST");
            wmtsOptions.setFormat("image/png");
            wmtsOptions.setMatrixSet("EPSG:2056");
            wmtsOptions.setStyle("default");
            wmtsOptions.setProjection(projection);
            wmtsOptions.setWrapX(true);
            wmtsOptions.setTileGrid(createWmtsTileGrid(projection, resolutionsSogis));

            Wmts wmtsSource = new Wmts(wmtsOptions);

            LayerOptions wmtsLayerOptions = OLFactory.createOptions();
            wmtsLayerOptions.setSource(wmtsSource);

            sogisWmtsLayer = new Tile(wmtsLayerOptions);
            sogisWmtsLayer.setOpacity(1.0);  
        }
        
        // Geoview BL WMS
        Tile geoviewBlWmsLayer;
        {
            TileWmsParams imageWMSParams = OLFactory.createOptions();
            imageWMSParams.setLayers("grundkarte_sw_group");
            imageWMSParams.set("FORMAT", "image/png");
            imageWMSParams.set("TRANSPARENT", "true");
            imageWMSParams.set("TILED", "true");

            TileWmsOptions imageWMSOptions = OLFactory.createOptions();
            imageWMSOptions.setUrl("https://geowms.bl.ch/");
            imageWMSOptions.setParams(imageWMSParams);

            TileWms imageWMSSource = new TileWms(imageWMSOptions);

            LayerOptions layerOptions = OLFactory.createOptions();
            layerOptions.setSource(imageWMSSource);

            geoviewBlWmsLayer = new ol.layer.Tile(layerOptions);
        }
       
        // Layergroup
        Group layerGroup = new Group();
        ol.Collection<Base> layers = new Collection<Base>();
        layers.push(geodiensteWmsLayer);
        //layers.push(geoviewBlWmsLayer);                
        //layers.push(sogisWmtsLayer);        
        layerGroup.setLayers(layers);
         
        ViewOptions viewOptions = OLFactory.createOptions();
        viewOptions.setProjection(projection);
        viewOptions.setResolutions(new double[] { 4000.0, 2000.0, 1000.0, 500.0, 250.0, 100.0, 50.0, 20.0, 10.0, 5.0, 2.5, 1.0, 0.5, 0.25, 0.1 });
        View view = new View(viewOptions);
        //Coordinate centerCoordinate = new Coordinate(2616491, 1240287);
        //Coordinate centerCoordinate = new Coordinate(2600593,1215639); // Messen
        //Coordinate centerCoordinate = new Coordinate(2600470,1215425); // Messen
        //Coordinate centerCoordinate = new Coordinate(2607752, 1228542); // Solothurn (Chantierwiese -> KbS) 
        //Coordinate centerCoordinate = new Coordinate(2723698,1211282); // Glarus
        //Coordinate centerCoordinate = new Coordinate(2723877,1211327); // Glarus
        //Coordinate centerCoordinate = new Coordinate(2688777,1283230); // Schaffhausen
        //Coordinate centerCoordinate = new Coordinate(2645218,1246759); // Unterentfelden
        //Coordinate centerCoordinate = new Coordinate(2683467,1248065); // Zürich
        //Coordinate centerCoordinate = new Coordinate(2660158,1183640); // Mittelpunkt CH
        Coordinate centerCoordinate = new Coordinate(2616491, 1240287); // Mittelpunkt SO


        view.setCenter(centerCoordinate);
        //view.setZoom(3);
      view.setZoom(6);
        //view.setZoom(14);

        MapOptions mapOptions = OLFactory.createOptions();
        mapOptions.setTarget(mapId);
        mapOptions.setView(view);
        mapOptions.setControls(new Collection<Control>());

        DefaultInteractionsOptions interactionOptions = new ol.interaction.DefaultInteractionsOptions();
        interactionOptions.setPinchRotate(false);
        mapOptions.setInteractions(Interaction.defaults(interactionOptions));

        Map map = new Map(mapOptions);
        //map.addLayer(wmsLayer);
        map.addLayer(layerGroup);
        
        return map;
    }
    
    public static Map getFederalMap(String mapId) {
        Proj4.defs("EPSG:2056", "+proj=somerc +lat_0=46.95240555555556 +lon_0=7.439583333333333 +k_0=1 +x_0=2600000 +y_0=1200000 +ellps=bessel +towgs84=674.374,15.056,405.346,0,0,0,0 +units=m +no_defs");

        ProjectionOptions projectionOptions = OLFactory.createOptions();
        projectionOptions.setCode("EPSG:2056");
        projectionOptions.setUnits("m");
        projectionOptions.setExtent(new Extent(2420000, 1030000, 2900000, 1350000));

        Projection projection = new Projection(projectionOptions);

        // Layergroup
        Group layerGroup = new Group();
        ol.Collection<Base> layers = new Collection<Base>();
        layerGroup.setLayers(layers);

        {
            WmtsOptions wmtsOptions = OLFactory.createOptions();
            wmtsOptions.setUrl("https://wmts.geo.admin.ch/1.0.0/{Layer}/default/current/2056/{TileMatrix}/{TileCol}/{TileRow}.jpeg");
            wmtsOptions.setLayer("ch.swisstopo.pixelkarte-grau");
            wmtsOptions.setRequestEncoding("REST");
            wmtsOptions.setProjection(projection);
            wmtsOptions.setWrapX(true);
            wmtsOptions.setTileGrid(createWmtsTileGrid(projection, resolutionsBgdi));

            Wmts wmtsSource = new Wmts(wmtsOptions);

            LayerOptions wmtsLayerOptions = OLFactory.createOptions();
            wmtsLayerOptions.setSource(wmtsSource);

            Tile wmtsLayer = new Tile(wmtsLayerOptions);
            wmtsLayer.setOpacity(1.0);
            wmtsLayer.setMinResolution(1.5);    
            
            layers.push(wmtsLayer);
        }
        
        {
            ImageWmsParams imageWMSParams = OLFactory.createOptions();
            imageWMSParams.setLayers("LCSF,LCSFPROJ,LCOBJ,SOSF,SOOBJ,SOLI,SOPT,Liegenschaften,Gebaeudeadressen,Nomenklatur,Rohrleitungen,Hoheitsgrenzen,Fixpunkte");
            imageWMSParams.set("FORMAT", "image/jpeg");
            //imageWMSParams.set("FORMAT", "image/png; mode=8bit");            
            imageWMSParams.set("TRANSPARENT", "false");
            imageWMSParams.set("TILED", "true");

            ImageWmsOptions imageWMSOptions = OLFactory.createOptions();
            imageWMSOptions.setUrl("https://wfs.geodienste.ch/av/deu");
            imageWMSOptions.setRatio(1.2f);
            imageWMSOptions.setParams(imageWMSParams);

            ImageWms imageWMSSource = new ImageWms(imageWMSOptions);

            LayerOptions layerOptions = OLFactory.createOptions();
            layerOptions.setSource(imageWMSSource);
            
            ol.layer.Image wmsLayer = new Image(layerOptions);
            wmsLayer.setOpacity(0.9);
            wmsLayer.setMaxResolution(1.5);
            layers.push(wmsLayer);
        }

        ViewOptions viewOptions = OLFactory.createOptions();
        viewOptions.setProjection(projection);
        viewOptions.setResolutions(new double[] { 4000.0, 2000.0, 1000.0, 500.0, 250.0, 100.0, 50.0, 20.0, 10.0, 5.0, 2.5, 1.0, 0.5, 0.25, 0.1 });
        View view = new View(viewOptions);
        //Coordinate centerCoordinate = new Coordinate(2616491, 1240287);
        Coordinate centerCoordinate = new Coordinate(2660158,1183640); // Mittelpunkt CH
        
        view.setCenter(centerCoordinate);
        //view.setZoom(6);
        view.setZoom(3);
        
        MapOptions mapOptions = OLFactory.createOptions();
        mapOptions.setTarget(mapId);
        mapOptions.setView(view);
        mapOptions.setControls(new Collection<Control>());

        DefaultInteractionsOptions interactionOptions = new ol.interaction.DefaultInteractionsOptions();
        interactionOptions.setPinchRotate(false);
        mapOptions.setInteractions(Interaction.defaults(interactionOptions));

        Map map = new Map(mapOptions);
        map.addLayer(layerGroup);
        
        return map;
    }
    
    public static Map getBlackAndWhiteMap(String mapId) {
        Proj4.defs("EPSG:2056", "+proj=somerc +lat_0=46.95240555555556 +lon_0=7.439583333333333 +k_0=1 +x_0=2600000 +y_0=1200000 +ellps=bessel +towgs84=674.374,15.056,405.346,0,0,0,0 +units=m +no_defs");

        ProjectionOptions projectionOptions = OLFactory.createOptions();
        projectionOptions.setCode("EPSG:2056");
        projectionOptions.setUnits("m");
        projectionOptions.setExtent(new Extent(2420000, 1030000, 2900000, 1350000));

        Projection projection = new Projection(projectionOptions);

        WmtsOptions wmtsOptions = OLFactory.createOptions();
        wmtsOptions.setUrl("https://geo.so.ch/api/wmts/1.0.0/{Layer}/default/2056/{TileMatrix}/{TileRow}/{TileCol}");
        wmtsOptions.setLayer("ch.so.agi.hintergrundkarte_sw");
        wmtsOptions.setRequestEncoding("REST");
        wmtsOptions.setFormat("image/png");
        wmtsOptions.setMatrixSet("EPSG:2056");
        wmtsOptions.setStyle("default");
        wmtsOptions.setProjection(projection);
        wmtsOptions.setWrapX(true);
        wmtsOptions.setTileGrid(createWmtsTileGrid(projection, resolutionsSogis));

        Wmts wmtsSource = new Wmts(wmtsOptions);

        LayerOptions wmtsLayerOptions = OLFactory.createOptions();
        wmtsLayerOptions.setSource(wmtsSource);

        Tile wmtsLayer = new Tile(wmtsLayerOptions);
        wmtsLayer.setOpacity(1.0);

        ViewOptions viewOptions = OLFactory.createOptions();
        viewOptions.setProjection(projection);
        viewOptions.setResolutions(new double[] { 4000.0, 2000.0, 1000.0, 500.0, 250.0, 100.0, 50.0, 20.0, 10.0, 5.0, 2.5, 1.0, 0.5, 0.25, 0.1 });
        View view = new View(viewOptions);
        Coordinate centerCoordinate = new Coordinate(2616491, 1240287);
//        Coordinate centerCoordinate = new Coordinate(2600593,1215639); // Messen
//        Coordinate centerCoordinate = new Coordinate(2600470,1215425); // Messen
//        Coordinate centerCoordinate = new Coordinate(2626873,1241448); // Egerkingen 1293 

        view.setCenter(centerCoordinate);
        view.setZoom(6);
//        view.setZoom(13);
        

        MapOptions mapOptions = OLFactory.createOptions();
        mapOptions.setTarget(mapId);
        mapOptions.setView(view);
        mapOptions.setControls(new Collection<Control>());

        DefaultInteractionsOptions interactionOptions = new ol.interaction.DefaultInteractionsOptions();
        interactionOptions.setPinchRotate(false);
        mapOptions.setInteractions(Interaction.defaults(interactionOptions));

        Map map = new Map(mapOptions);
        map.addLayer(wmtsLayer);
        
        return map;
    }
    
    private static TileGrid createWmtsTileGrid(Projection projection, double[] resolutions) {
        WmtsTileGridOptions wmtsTileGridOptions = OLFactory.createOptions();
        
        String[] matrixIds = new String[resolutions.length];

        for (int z = 0; z < resolutions.length; ++z) {
            matrixIds[z] = String.valueOf(z);
        }

        Coordinate tileGridOrigin = projection.getExtent().getTopLeft();
        wmtsTileGridOptions.setOrigin(tileGridOrigin);
        wmtsTileGridOptions.setResolutions(resolutions);
        wmtsTileGridOptions.setMatrixIds(matrixIds);

        return new WmtsTileGrid(wmtsTileGridOptions);
    }
}