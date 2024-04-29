package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.PartialCodeRequest;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public interface FDADataService {
     JsonNode getAllIndustry() throws IOException;
     JsonNode getByIndustryId(Integer id) throws IOException;
     JsonNode getSubClassData() throws IOException;
     JsonNode getBySubclassId(String id) throws IOException;
     JsonNode getByIndustrySubclassId(String id) throws IOException;
     JsonNode fetchClass() throws IOException;
     JsonNode getByClassId(String id) throws IOException;
     JsonNode getByIndustryClassId(String id) throws IOException;
     JsonNode getPic() throws IOException;
     JsonNode getByPicId(String id) throws IOException;
     JsonNode getByIndustryPic(String id) throws IOException;
     JsonNode getProduct() throws IOException;
     JsonNode getByProductId(String id) throws IOException;
     JsonNode getByProductName(String name) throws IOException;
     JsonNode getProductNameByFormData(String name) throws IOException;
     JsonNode getByIndustryProductByIndustryId(String id) throws IOException;
     JsonNode validateProductCode(String code) throws IOException;
     JsonNode getProductCodeByIndustry(String id) throws IOException;
     JsonNode getCodes(PartialCodeRequest request) throws IOException;
}
