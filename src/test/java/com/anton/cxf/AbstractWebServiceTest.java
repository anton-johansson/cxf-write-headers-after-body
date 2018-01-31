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

import static java.util.Arrays.asList;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;

/**
 * Abstract skeleton for web service tests.
 */
public abstract class AbstractWebServiceTest extends Assert
{
    protected static final int PORT = 1337;
    protected ClientAndServer server;

    @Before
    public void setUp() throws Exception
    {
        server = startClientAndServer(PORT);
    }

    @After
    public void tearDown()
    {
        server.stop();
    }

    /**
     * Gets the sent requests for the current test.
     *
     * @return Returns the sent requests.
     */
    protected List<HttpRequest> getSentRequests()
    {
        return asList(server.retrieveRecordedRequests(null));
    }
}
