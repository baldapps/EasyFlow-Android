package com.apipas.easyflow.android.call;


import com.apipas.easyflow.android.FlowContext;

/**
 * User: andrey
 * Date: 12/03/13
 * Time: 7:29 PM
 */
public interface ContextHandler {
    void call(FlowContext context) throws Exception;
}
