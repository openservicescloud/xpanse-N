/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Response for the REST API.
 */
@Data
public class Response {

    @NotNull
    private String code;
    @NotNull
    private String message;
    @NotNull
    private Boolean success;

    private Object data;

    /**
     * Constructor.
     *
     * @param errCode errCode.
     * @param message message.
     */
    public Response(ErrCode errCode, String message) {
        this.code = errCode.getCode();
        this.message = errCode.getErrMsg() + ". -- " + message;
        this.success = false;
    }

    public Response() {
    }

    /**
     * result.
     *
     * @param message message.
     * @return Response response.
     */
    public static Response successResponse(String message) {
        Response response = new Response();
        response.setSuccess(true);
        response.setCode("Success.0000");
        response.setMessage(message);
        return response;
    }

    /**
     * result.
     *
     * @param message message.
     * @param data    data.
     * @return Response response.
     */
    public static Response resultResponse(String message, Object data) {
        Response response = new Response();
        response.setSuccess(true);
        response.setCode("Success.0000");
        response.setMessage(message);
        response.setData(data);
        return response;
    }

}