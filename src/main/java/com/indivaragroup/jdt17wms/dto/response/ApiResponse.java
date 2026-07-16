package com.indivaragroup.jdt17wms.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indivaragroup.jdt17wms.dto.utils.ApiSuccess;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ApiResponse<T> {

    @JsonProperty("code")
    private int restApiResponseHttpCode;

    @JsonProperty("result")
    private T restApiResponseResult;

    @JsonProperty("message")
    private String restApiResponseMessage;

    @JsonProperty("error")
    private Map<String, Serializable> restApiResponseError;


    public static <T> ApiResponse<T> success(ApiSuccess success, T result) {
        return ApiResponse.<T>builder()
                .restApiResponseHttpCode(success.getCode())
                .restApiResponseMessage(success.getMessage())
                .restApiResponseResult(result)
                .restApiResponseError(null)
                .build();
    }

    public static <T> ApiResponse<T> created(ApiSuccess success, T result) {
        return ApiResponse.<T>builder()
                .restApiResponseHttpCode(success.getCode())
                .restApiResponseMessage(success.getMessage())
                .restApiResponseResult(result)
                .restApiResponseError(null)
                .build();
    }
}
