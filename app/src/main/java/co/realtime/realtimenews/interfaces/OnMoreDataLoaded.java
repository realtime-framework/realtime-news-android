package co.realtime.realtimenews.interfaces;

import java.util.ArrayList;

import co.realtime.realtimenews.domains.ContentResponse;

public interface OnMoreDataLoaded {

    void onMoreDataLoaded(ArrayList<ContentResponse> newContent);

}
