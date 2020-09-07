package lab.idioglossia.row.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lab.idioglossia.row.client.callback.HttpExtendedResponseCallback;
import lab.idioglossia.row.client.callback.ResponseCallback;
import lab.idioglossia.row.client.model.RowRequest;
import lab.idioglossia.row.client.model.RowResponse;
import lab.idioglossia.row.client.model.protocol.RowResponseStatus;
import lombok.SneakyThrows;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Map;

public class RestTemplateRowHttpClient implements RowHttpClient {
    private final String address;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RestTemplateRowHttpClient(String address, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.address = address;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    @Override
    public void sendRequest(RowRequest<?, ?> rowRequest, ResponseCallback<?> responseCallback) {
        HttpExtendedResponseCallback httpExtendedResponseCallback = (HttpExtendedResponseCallback) responseCallback;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if(rowRequest != null)
            rowRequest.getHeaders().forEach(headers::set);

        HttpEntity<String> entity = new HttpEntity<String>(objectMapper.writeValueAsString(rowRequest.getBody()), headers);
        URI uri = getUri(rowRequest);
        try {
            ResponseEntity responseEntity = restTemplate.exchange(uri, getHttpMethod(rowRequest.getMethod()), entity, httpExtendedResponseCallback.getResponseBodyClass());
            httpExtendedResponseCallback.onResponse(getRowResponse(responseEntity));
        }catch (Exception e){
            httpExtendedResponseCallback.onError(e);
        }
    }

    protected URI getUri(RowRequest<?, ?> rowRequest) throws IOException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(address)
                .path(rowRequest.getAddress());

        if (rowRequest.getQuery() != null) {
            Map<String, Object> query = getObjectAsMap(objectMapper);
            query.forEach(builder::queryParam);
        }
        UriComponents uriComponents = builder.build();
        return uriComponents.toUri();
    }

    protected RowResponse getRowResponse(ResponseEntity responseEntity) {
        RowResponse rowResponse = new RowResponse();
        rowResponse.setBody(responseEntity.getBody());
        rowResponse.setHeaders(responseEntity.getHeaders().toSingleValueMap());
        rowResponse.setStatus(RowResponseStatus.OK);
        return rowResponse;
    }

    private HttpMethod getHttpMethod(RowRequest.RowMethod method) {
        switch (method){
            case POST:
                return HttpMethod.POST;
            case GET:
                return HttpMethod.GET;
            case PUT:
                return HttpMethod.PUT;
            case PATCH:
                return HttpMethod.PATCH;
            case DELETE:
                return HttpMethod.DELETE;
            case FETCH:
                throw new RuntimeException("Http Method Fetch is not supported");
            default:
                return HttpMethod.GET;
        }
    }

    private Map<String, Object> getObjectAsMap(Object object) throws IOException {
        StringWriter sw = new StringWriter();
        objectMapper.writeValue(sw, object);
        return objectMapper.readValue(sw.toString(), Map.class);
    }
}
