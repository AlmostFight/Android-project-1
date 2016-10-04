package com.example.tijmenvangroezen.testproject;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by tijmenvangroezen on 03-10-16.
 */

public class FestivalEvent
{
    public String title;
    public String description;
    public String description_teaser;
    public String genre;
    public String website;
    public Image[] images; /* You must register the custom ImagesDeserializer (below) to
                                parse this attribute correctly */

    public FestivalEvent(String festivalTitle, String description)
    {
        this.title = festivalTitle;
        this.description_teaser = description;
    }

    private List<String> eventList = new ArrayList<>();

    public List<String> getEventData()
    {
        eventList.add(title);
        eventList.add(description);
        eventList.add(description_teaser);
        eventList.add(genre);
        eventList.add(website);
        eventList.add(getThumbUrl());

        return eventList;
    }


    /**
     * Returns the url of the first image that has type thumb
     */
    public String getThumbUrl()
    {
        for (Image image : images)
        {
            if (image.type.equals("thumb"))
            {
                return image.versions.square.url;
            }
        }
        return "some default url to an image with a red cross indicating that we have no image for you";
    }

    /** This class doesn't correspond to any key in the JSON of the festival API.
     * Register the ImagesDeserializer as a typeAdapter at the GSONBuilder
     *
     *
     *  Gson gson = new GsonBuilder()
     *        .registerTypeAdapter(FestivalEvent.Image[].class,
     *        new FestivalEvent.ImagesDeserializer())
     *        .create();
     *
     *  now call  gson.fromJson(the response object with the data, the class you need) to
     *  obtain the data
     * **/
    public static class Image
    {
        public Versions versions;
        public String type;

        public Image(String type, Versions versions)
        {
            this.versions = versions;
            this.type = type;
        }
    }

    public static class Versions
    {
        @SerializedName("large-1024")
        public ImageVersion large;

        @SerializedName("square-75")
        public ImageVersion square;
    }

    public static class ImageVersion
    {
        public String url;
    }

    /**
     * The festival api returns the images as a object with hash-keys with each hash-key holding
     * all data (and versions) of a particular image.
     * Because each hash-key is unique we need some custom deserialization to get all images
     * in an array.
     */
    public static class ImagesDeserializer implements JsonDeserializer<Image[]>
    {
        @Override

        public FestivalEvent.Image[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException
        {

            ArrayList<Image> festivalImagesList = new ArrayList<>();

            /* Iterate over all images (the hashes )in the Json data and put the versions data
            in a new arraylist */
            Iterator<Map.Entry<String, JsonElement>> it = json.getAsJsonObject().entrySet().iterator();
            while(it.hasNext())
            {

                /* Grab one image node */
                JsonObject imageJson = it.next().getValue().getAsJsonObject();

                /* Get the type node */
                JsonElement jsonTypeData = imageJson.get("type");
                /* Get the data in the type node. Luckily this data is just a string */
                String imageType = context.deserialize(jsonTypeData, String.class);

                /* Get all versions node*/
                JsonElement jsonVersionsData = imageJson.get("versions");
                /* The data in the versions node is another JSON object with element so we need to traverse
                * these nodes as well. Luckily we can just use the default GSON deserializer providing
                * our Versions class defined above */
                FestivalEvent.Versions imageVersions =
                        context.deserialize(jsonVersionsData, FestivalEvent.Versions.class);

                /* Create a new Festival.Image node object and add it to the list of images*/
                FestivalEvent.Image festivalImage = new FestivalEvent.Image(imageType, imageVersions);
                festivalImagesList.add(festivalImage);
            }

            return festivalImagesList.toArray(new FestivalEvent.Image[festivalImagesList.size()]);
        }
    }



}
