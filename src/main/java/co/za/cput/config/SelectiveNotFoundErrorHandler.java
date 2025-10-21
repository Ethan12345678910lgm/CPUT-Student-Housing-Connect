package co.za.cput.config;

import java.io.IOException;
import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

public class SelectiveNotFoundErrorHandler implements ResponseErrorHandler {

    public static final String TRIGGER_HEADER = "X-Trigger-Client-Error";

    private final DefaultResponseErrorHandler delegate = new DefaultResponseErrorHandler();

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return shouldTrigger(response);
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (shouldTrigger(response)) {
            delegate.handleError(response);
        }
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        if (shouldTrigger(response)) {
            delegate.handleError(url, method, response);
        }
    }

    private boolean shouldTrigger(ClientHttpResponse response) throws IOException {
        HttpHeaders headers = response.getHeaders();
        return response.getStatusCode() == HttpStatus.NOT_FOUND && headers.containsKey(TRIGGER_HEADER);
    }
}