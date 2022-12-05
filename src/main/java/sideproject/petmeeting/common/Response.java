package sideproject.petmeeting.common;

import lombok.Data;

@Data
public class Response {

    private StatusEnum status;
    private String message;
    private Object data;

    // Initial Setting
    public Response() {
        this.status = StatusEnum.BAD_REQUEST;
        this.message = null;
        this.data = null;
    }
}
