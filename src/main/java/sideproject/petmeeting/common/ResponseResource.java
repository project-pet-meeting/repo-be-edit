package sideproject.petmeeting.common;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.hateoas.EntityModel;

public class ResponseResource extends EntityModel<Object> {
    @JsonUnwrapped
    private Object object;

    public ResponseResource(Object object) {
        this.object = object;
    }

    public Object getObject() {
        return object;
    }
}
