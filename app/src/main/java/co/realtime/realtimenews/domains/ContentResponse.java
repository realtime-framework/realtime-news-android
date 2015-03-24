package co.realtime.realtimenews.domains;

import android.view.View;

import co.realtime.realtimenews.R;

public class ContentResponse {

    private String monthYear;
    private String type;
    private String url;
    private String title;
    private String img;
    private String body;
    private String description;
    private String tag;
    private String timestamp;
    private boolean isNew;
    private boolean isUpdated;
    private boolean isClickable;
    private String stateText;
    private String timestampText;
    private int stateVisibility = View.INVISIBLE;
    private int saveBtnResource = R.drawable.download;

    public String getMonthYear() { return monthYear;}

    public void setMonthYear(String monthYear) {this.monthYear = monthYear;}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public void setUpdated(boolean isUpdated) {
        this.isUpdated = isUpdated;
    }

    public String getStateText() {
        return stateText;
    }

    public void setStateText(String stateText) {
        this.stateText = stateText;
    }

    public int getStateVisibility() {
        return stateVisibility;
    }

    public void setStateVisibility(int stateVisibility) {
        this.stateVisibility = stateVisibility;
    }

    public int getSaveBtnResource() {
        return saveBtnResource;
    }

    public void setSaveBtnResource(int saveBtnResource) {
        this.saveBtnResource = saveBtnResource;
    }

    public boolean isClickable() {
        return isClickable;
    }

    public void setClickable(boolean isClickable) {
        this.isClickable = isClickable;
    }

    public String getTimestampText() {
        return timestampText;
    }

    public void setTimestampText(String timestampText) {
        this.timestampText = timestampText;
    }
}

