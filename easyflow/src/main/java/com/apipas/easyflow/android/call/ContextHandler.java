package com.apipas.easyflow.android.call;


import com.apipas.easyflow.android.StatefulContext;

/**
 * User: andrey
 * Date: 12/03/13
 * Time: 7:29 PM
 */
public interface ContextHandler<C extends StatefulContext> {
    void call(C context) throws Exception;
}
