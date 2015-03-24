package co.realtime.realtimenews.domains;

import java.io.Serializable;

public class TagResponse implements Serializable{
    private String tag;
    private String type;


    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
