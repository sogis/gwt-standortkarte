package ch.so.agi.standortkarte;

import java.io.Serializable;
import java.util.List;

public class SearchResult implements Serializable {
    private String display;
    private String dataproductId;
    private int featureId;
    private String idFieldName;
    private String egrid;
    private List<Double> bbox;
    private String type;

    public SearchResult() {
    }

    public SearchResult(String display) {
        this.display = display;
    }

    public String getLabel() {
        return display;
    }

    public void setLabel(String label) {
        this.display = label;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getDataproductId() {
        return dataproductId;
    }

    public void setDataproductId(String dataproductId) {
        this.dataproductId = dataproductId;
    }

    public int getFeatureId() {
        return featureId;
    }

    public void setFeatureId(int featureId) {
        this.featureId = featureId;
    }

    public String getIdFieldName() {
        return idFieldName;
    }

    public void setIdFieldName(String idFieldName) {
        this.idFieldName = idFieldName;
    }

    public String getEgrid() {
        return egrid;
    }

    public void setEgrid(String egrid) {
        this.egrid = egrid;
    }

    public List<Double> getBbox() {
        return bbox;
    }

    public void setBbox(List<Double> bbox) {
        this.bbox = bbox;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
