package com.learning.roles.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
public class HttpResponse {

    private int httpStatusCode;
    private HttpStatus httpStatus;
    private String reason;
    private String message;
    private Map<String, Object> errors;

    @JsonFormat(shape = JsonFormat.Shape.STRING,
            pattern = "MM-dd-yyyy HH:mm:ss aa",
            timezone = "America/Bogota",
            locale = "es-CO")
    private Date timeStamp;

    public HttpResponse(int httpStatusCode, HttpStatus httpStatus,
                        String reason, String message) {
        this.httpStatusCode = httpStatusCode;
        this.httpStatus = httpStatus;
        this.reason = reason;
        this.message = message;
        this.timeStamp = new Date();
    }
    public HttpResponse(int httpStatusCode, HttpStatus httpStatus,
                        String reason, String message,
                        Map<String, Object> errors) {
        this.httpStatusCode = httpStatusCode;
        this.httpStatus = httpStatus;
        this.reason = reason;
        this.message = message;
        this.timeStamp = new Date();
        this.errors = errors;
    }

}
