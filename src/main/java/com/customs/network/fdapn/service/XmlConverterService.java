package com.customs.network.fdapn.service;

import com.customs.network.fdapn.model.CustomerDetails;

import javax.xml.bind.*;
import java.io.StringWriter;

public class XmlConverterService {
    public static String convertToXml(CustomerDetails customerDetails) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(CustomerDetails.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter sw = new StringWriter();
        marshaller.marshal(customerDetails, sw);
        return sw.toString();
    }
}
