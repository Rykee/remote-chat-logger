package hu.rhykee.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class HttpMessageSender {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .build();

    private HttpMessageSender() {
    }

    public static HttpMessageSender getInstance() {
        return Holder.INSTANCE;
    }

    public <T> void sendPostHttpMessage(String url, T body, String authorization) {
        try (CloseableHttpClient httpClient = createHttpClient()) {
            httpClient.execute(createPostRequest(url, body, authorization));
        } catch (Exception e) {
            log.error("Failed to send HTTP POST message with url: {} and authorization {}.\nBody content: {}", url, authorization, body, e);
        }
    }

    public <T> void sendDeleteHttpMessage(String url, T body, String authorization) {
        try (CloseableHttpClient httpClient = createHttpClient()) {
            httpClient.execute(createDeleteRequest(url, body, authorization));
        } catch (Exception e) {
            log.error("Failed to send HTTP DELETE message with url: {} and authorization {}.\nBody content: {}", url, authorization, body, e);
        }
    }

    public <T> HttpUriRequest createPostRequest(String url, T body, String authorization) throws JsonProcessingException {
        HttpUriRequest request = RequestBuilder.post(url)
                .setEntity(new StringEntity(MAPPER.writeValueAsString(body), ContentType.APPLICATION_JSON))
                .build();
        addAuthorizationIfPresent(request, authorization);
        return request;
    }

    public <T> HttpUriRequest createDeleteRequest(String url, T body, String authorization) throws JsonProcessingException {
        HttpUriRequest request = RequestBuilder.delete(url)
                .setEntity(new StringEntity(MAPPER.writeValueAsString(body), ContentType.APPLICATION_JSON))
                .build();
        addAuthorizationIfPresent(request, authorization);
        return request;
    }

    private CloseableHttpClient createHttpClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(null, (chain, authType) -> true)
                .build();
        return HttpClients
                .custom()
                .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
                .build();
    }

    private void addAuthorizationIfPresent(HttpUriRequest request, String authorization) {
        if (StringUtils.isNotBlank(authorization)) {
            request.addHeader("Authorization", authorization);
        }
    }

    private static class Holder {
        private static final HttpMessageSender INSTANCE = new HttpMessageSender();
    }

}
