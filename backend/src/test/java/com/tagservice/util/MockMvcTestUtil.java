package com.tagservice.util;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;

/**
 * Utility class for testing REST APIs using REST Assured MockMvc.
 * Encapsulates MockMvc and provides convenient methods for HTTP operations.
 */
public class MockMvcTestUtil {

    public MockMvcTestUtil(MockMvc mockMvc) {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    /**
     * Creates a GET request builder.
     *
     * @param path the request path
     * @return the request builder
     */
    public RequestBuilder get(String path) {
        return new RequestBuilder(path, "GET");
    }

    /**
     * Creates a POST request builder.
     *
     * @param path the request path
     * @return the request builder
     */
    public RequestBuilder post(String path) {
        return new RequestBuilder(path, "POST");
    }

    /**
     * Creates a PUT request builder.
     *
     * @param path the request path
     * @return the request builder
     */
    public RequestBuilder put(String path) {
        return new RequestBuilder(path, "PUT");
    }

    /**
     * Creates a DELETE request builder.
     *
     * @param path the request path
     * @return the request builder
     */
    public RequestBuilder delete(String path) {
        return new RequestBuilder(path, "DELETE");
    }

    /**
     * Creates a PATCH request builder.
     *
     * @param path the request path
     * @return the request builder
     */
    public RequestBuilder patch(String path) {
        return new RequestBuilder(path, "PATCH");
    }

    /**
     * Fluent builder for configuring HTTP requests.
     */
    public static class RequestBuilder {
        private final String path;
        private final String method;
        private Object body;
        private MediaType contentType;
        private String headerName;
        private String headerValue;
        private String queryParamName;
        private Object queryParamValue;
        private String pathParamName;
        private Object pathParamValue;

        private RequestBuilder(String path, String method) {
            this.path = path;
            this.method = method;
        }

        /**
         * Sets the request body.
         *
         * @param body the request body object
         * @return this builder
         */
        public RequestBuilder body(Object body) {
            this.body = body;
            return this;
        }

        /**
         * Sets the Content-Type header.
         *
         * @param contentType the content type
         * @return this builder
         */
        public RequestBuilder contentType(MediaType contentType) {
            this.contentType = contentType;
            return this;
        }

        /**
         * Sets the Content-Type header to application/json.
         *
         * @return this builder
         */
        public RequestBuilder jsonContent() {
            this.contentType = MediaType.APPLICATION_JSON;
            return this;
        }

        /**
         * Adds a header to the request.
         *
         * @param name  the header name
         * @param value the header value
         * @return this builder
         */
        public RequestBuilder header(String name, String value) {
            this.headerName = name;
            this.headerValue = value;
            return this;
        }

        /**
         * Adds a query parameter to the request.
         *
         * @param name  the parameter name
         * @param value the parameter value
         * @return this builder
         */
        public RequestBuilder queryParam(String name, Object value) {
            this.queryParamName = name;
            this.queryParamValue = value;
            return this;
        }

        /**
         * Adds a path parameter to the request.
         *
         * @param name  the parameter name
         * @param value the parameter value
         * @return this builder
         */
        public RequestBuilder pathParam(String name, Object value) {
            this.pathParamName = name;
            this.pathParamValue = value;
            return this;
        }

        /**
         * Executes the request and returns the response for assertions.
         *
         * @return the response
         */
        public io.restassured.module.mockmvc.response.ValidatableMockMvcResponse then() {
            var requestSpec = given();
            
            if (contentType != null) {
                requestSpec.contentType(contentType);
            }
            if (body != null) {
                requestSpec.body(body);
            }
            if (headerName != null && headerValue != null) {
                requestSpec.header(headerName, headerValue);
            }
            if (queryParamName != null && queryParamValue != null) {
                requestSpec.queryParam(queryParamName, queryParamValue);
            }
            if (pathParamName != null && pathParamValue != null) {
                requestSpec.pathParam(pathParamName, pathParamValue);
            }

            return switch (method) {
                case "GET" -> requestSpec.when().get(path).then();
                case "POST" -> requestSpec.when().post(path).then();
                case "PUT" -> requestSpec.when().put(path).then();
                case "DELETE" -> requestSpec.when().delete(path).then();
                case "PATCH" -> requestSpec.when().patch(path).then();
                default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            };
        }
    }
}
