package com.customs.network.fdapn.utils;

import com.customs.network.fdapn.model.TrackingDetails;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "TrackingDetailsList")
public class TrackingDetailsListWrapper {
    private List<TrackingDetails> trackingDetailsList;

    @XmlElement(name = "TrackingDetails")
    public List<TrackingDetails> getTrackingDetailsList() {
        return trackingDetailsList;
    }

    public void setTrackingDetailsList(List<TrackingDetails> trackingDetailsList) {
        this.trackingDetailsList = trackingDetailsList;
    }
}
