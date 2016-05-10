package com.alpine.plugins;

import javax.ws.rs.core.Response;

/**
 * Created by jasonmiller on 5/10/16.
 */
public enum Responses {
    ;


    public static final Response NOT_FOUND = Response.status(Response.Status.NOT_FOUND).build();
    public static final Response CONFLICT = Response.status(Response.Status.CONFLICT).build();
    public static final Response ACCEPTED = Response.accepted().build();
}
