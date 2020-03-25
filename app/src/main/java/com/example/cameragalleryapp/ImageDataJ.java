package com.example.cameragalleryapp;// This class stores the operational data for a particular machine within the Android App RAM

import org.json.JSONException;
import org.json.JSONObject;

public class ImageDataJ {
    /**
     *
     */
    
    private static final long serialVersionUID = 1L;
    public String timeStamp;
    public String locationStampLat;
    public String locationStampLong;
    public String caption;

    /**
     * Convert the Object Variables into JSON object.
     *
     * @return jsonObject
     *            Image Data.
     */
    public JSONObject toJson() throws JSONException
    {
        // Create a dictionary of details
        JSONObject details_json = new JSONObject();
        details_json.put("timeStamp", this.timeStamp);
        details_json.put("locationStampLat", this.locationStampLat);
        details_json.put("locationStampLong", this.locationStampLong);
        details_json.put("caption", this.caption);

        // Object name
        JSONObject object_json = new JSONObject();
        object_json.put("imageData", details_json);

        return object_json;
    }


    /**
     * Update the Object Variables from JSON object. The datatype conversion is taken care of here.
     *
     * @param jsonObject
     *            operational data details object.
     */
    public void update(JSONObject jsonObject) throws JSONException
    {
        final JSONObject details_json = (JSONObject) jsonObject.get("imageData");

        this.timeStamp = (String) details_json.get("timeStamp");
        this.locationStampLat = (String) details_json.get("locationStampLat");
        this.locationStampLong = (String)details_json.get("locationStampLong");
        this.caption = (String) details_json.get("caption");
    
    }
}
