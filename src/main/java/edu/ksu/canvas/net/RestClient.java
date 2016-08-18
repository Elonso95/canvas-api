package edu.ksu.canvas.net;

import edu.ksu.canvas.exception.InvalidOauthTokenException;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Map;

public interface RestClient {
    Response sendApiGet(@NotNull String token, @NotNull String url, int connectTimeout, int readTimeout) throws IOException;
    Response sendJsonPost(@NotNull String token, @NotNull String url, String json, int connectTimeout, int readTimeout) throws IOException;
    Response sendJsonPut(@NotNull String token, @NotNull String url, String json, int connectTimeout, int readTimeout) throws IOException;
    Response sendApiPost(@NotNull String token, @NotNull String url, Map<String, String> postParameters, int connectTimeout, int readTimeout) throws InvalidOauthTokenException, IOException;
    Response sendApiDelete(@NotNull String token, @NotNull String url, Map<String, String> deleteParameters, int connectTimeout, int readTimeout) throws InvalidOauthTokenException, IOException;
    Response sendApiPut(@NotNull String token, @NotNull String url, Map<String, Object> putParameters, int connectTimeout, int readTimeout) throws InvalidOauthTokenException, IOException;
}
