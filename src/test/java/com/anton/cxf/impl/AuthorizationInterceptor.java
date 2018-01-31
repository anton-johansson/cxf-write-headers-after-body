/**
 * Copyright 2018 Anton Johansson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.anton.cxf.impl;

import static com.anton.cxf.impl.WebClientFactory.OUTGOING_BODY_KEY;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.cxf.message.Message.PROTOCOL_HEADERS;
import static org.apache.cxf.phase.Phase.SEND;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

/**
 * An interceptor that adds an {@code Authorization} header by looking at the body of the output.
 */
public class AuthorizationInterceptor extends AbstractPhaseInterceptor<Message>
{
    private final String prefix;

    public AuthorizationInterceptor(String prefix)
    {
        super(SEND);
        this.prefix = prefix;
    }

    @Override
    public void handleMessage(Message message) throws Fault
    {
        String body = getBody(message);
        String authorization = getAuthorization(body);

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> headers = (MultivaluedMap<String, String>) message.get(PROTOCOL_HEADERS);
        headers.putSingle(AUTHORIZATION, authorization);
    }

    private String getBody(Message message)
    {
        String body = (String) message.get(OUTGOING_BODY_KEY);
        return defaultIfNull(body, "");
    }

    /**
     * Dummy implementation. In a real world scenario, you'd take the body together with a secret key and has the value in some predefined way.
     */
    private String getAuthorization(String body)
    {
        return prefix + " " + body.length();
    }
}
