package org.loadtester.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

public class HttpClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public <T> T get(String url, Class<T> responseType) {
        return request(HttpMethod.GET, url, null,null, responseType);
    }

    public <T> T get(String url, HttpHeaders headers, Class<T> responseType) {
        return request(HttpMethod.GET, url, headers,null, responseType);
    }

    public void post(String url, Object requestBody) {
        request(HttpMethod.POST, url, null, requestBody, String.class);
    }

    public void post(String url, HttpHeaders headers, Object requestBody) {
        request(HttpMethod.POST, url, headers, requestBody, String.class);
    }

    public void put(String url, Object requestBody) {
        request(HttpMethod.PUT, url, null, requestBody, String.class);
    }

    public void delete(String url) {
        request(HttpMethod.DELETE, url, null,null, String.class);
    }

    public <T> List<T> fetchList(String url, Class<T> elementType) {
        try {
            String json = get(url, String.class);
            JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, elementType);
            return objectMapper.readValue(json, listType);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("JSON 파싱 실패: " + url, ex);
        }
    }

    public <T> T fetchAndParse(String url, HttpHeaders headers, TypeReference<T> typeRef) {
        try {
            String json = get(url, headers, String.class);
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("JSON 파싱 실패: " + url, ex);
        }
    }

    private <T> T request(HttpMethod httpMethod, String url, HttpHeaders headers, Object requestBody, Class<T> responseType) {
        try {
            HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    new URI(url), httpMethod, requestEntity, String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("HTTP 응답 코드 오류: " + response.getStatusCode());
            }

            if (responseType == String.class) {
                return responseType.cast(response.getBody());
            }
            return parseBody(response.getBody(), responseType);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T parseBody(String body, Class<T> responseType) throws Exception {
        if (body == null || body.trim().isEmpty()) {
            throw new Exception("응답 본문이 비어 있습니다.");
        }

        return objectMapper.readValue(body, responseType);
    }
}
