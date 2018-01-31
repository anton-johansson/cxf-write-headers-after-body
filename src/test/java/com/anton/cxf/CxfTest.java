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
package com.anton.cxf;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.xml.bind.annotation.XmlAccessType.FIELD;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Test;
import org.mockserver.model.HttpRequest;

import com.anton.cxf.impl.AuthorizationInterceptor;
import com.anton.cxf.impl.WebClientFactory;

/**
 * Tests some parts of CXF.
 */
public class CxfTest extends AbstractWebServiceTest
{
    @Test
    public void testWithChunkedNotExceedingFirstChunk()
    {
        WebClient client = WebClientFactory.of("http://localhost:" + PORT)
                .accept(APPLICATION_JSON)
                .type(APPLICATION_JSON)
                .serializationWithJson()
                .allowChunking(100)
                .storeOutgoingContents()
                .outInterceptor(new AuthorizationInterceptor("Anton"))
                .build();

        client.post(model(50));
        assertAuthorizationHeader("Anton 64"); // 14 bytes are JSON meta-data
    }

    @Test
    public void testWithChunkedExceedingFirstChunk()
    {
        WebClient client = WebClientFactory.of("http://localhost:" + PORT)
                .accept(APPLICATION_JSON)
                .type(APPLICATION_JSON)
                .serializationWithJson()
                .allowChunking(100)
                .storeOutgoingContents()
                .outInterceptor(new AuthorizationInterceptor("Anton"))
                .build();

        client.post(model(150));
        assertAuthorizationHeader("Anton 164"); // 14 bytes are JSON meta-data
    }

    @Test
    public void testWithoutChunked()
    {
        WebClient client = WebClientFactory.of("http://localhost:" + PORT)
                .accept(APPLICATION_JSON)
                .type(APPLICATION_JSON)
                .serializationWithJson()
                .storeOutgoingContents()
                .outInterceptor(new AuthorizationInterceptor("Anton"))
                .build();

        client.post(model(150));
        assertAuthorizationHeader("Anton 164"); // 14 bytes are JSON meta-data
    }

    private void assertAuthorizationHeader(String expected)
    {
        List<HttpRequest> requests = getSentRequests();
        assertEquals(1, requests.size());

        String actual = requests.get(0).getFirstHeader("Authorization");
        assertEquals(expected, actual);
    }

    private Model model(int sizeOfContent)
    {
        Model model = new Model();
        model.content = StringUtils.repeat('a', sizeOfContent);
        return model;
    }

    /**
     * A model for tests.
     */
    @XmlAccessorType(FIELD)
    private static class Model
    {
        @SuppressWarnings("unused")
        private String content;
    }
}
