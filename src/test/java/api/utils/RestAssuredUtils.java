package api.utils;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import utils.ConfigUtils;

public class RestAssuredUtils {
    private static final String BASE_URL = ConfigUtils.getProperty("api.base.url");

    // ThreadLocal ensures each thread gets its own RequestSpecification instance (useful for parallel execution)
    private static final ThreadLocal<RequestSpecification> requestSpec = ThreadLocal.withInitial(() ->
            new RequestSpecBuilder()
                    .setBaseUri(BASE_URL)
                    .build()
    );

    public static RequestSpecification getRequestSpec() {
        return requestSpec.get();
    }

    public static RequestSpecification getRequestSpecWithAuth(String apiKey) {
        return new RequestSpecBuilder()
                .addRequestSpecification(getRequestSpec())
                .addQueryParam("appid", apiKey)
                .build();
    }
}