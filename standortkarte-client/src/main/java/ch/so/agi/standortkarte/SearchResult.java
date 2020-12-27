package ch.so.agi.standortkarte;

import java.io.Serializable;
import java.util.List;

public class SearchResult implements Serializable {
    private String label;
    private double lon;
    private double lat;
   
    public SearchResult() {
    }
    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
}
