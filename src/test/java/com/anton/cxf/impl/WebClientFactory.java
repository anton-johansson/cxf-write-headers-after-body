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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;

import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * A factory that builds {@link WebClient web clients}.
 */
public final class WebClientFactory
{
    /** The key used to get the outgoing body of a CXF message. */
    public static final String OUTGOING_BODY_KEY = "com.viskan.cxf.bodyString";

    private WebClientFactory()
    {
    }

    /**
     * Begins creating a new {@link WebClient web clients}
     *
     * @param endpoint The endpoint to use, e.g. {@code "https://api.klarna.com"}.
     * @return Returns the builder.
     */
    public static WebClientBuilder of(String endpoint)
    {
        return new WebClientBuilder(endpoint);
    }

    /**
     * Builder for {@link WebClient web clients}.
     */
    public static class WebClientBuilder
    {
        private final List<Object> providers = new ArrayList<>();
        private final JAXRSClientFactoryBean bean;
        private MediaType[] accepts;
        private MediaType type;
        private boolean allowChunking;
        private int chunkingThreshold;

        private WebClientBuilder(String endpoint)
        {
            bean = new JAXRSClientFactoryBean();
            bean.setAddress(endpoint);
            bean.getFeatures().add(new LoggingFeature());
        }

        /**
         * Sets the {@code Accept} header for this client.
         *
         * @param accepts The accepted types.
         * @return Returns the builder, for chaining.
         */
        public WebClientBuilder accept(String... accepts)
        {
            this.accepts = Stream.of(accepts).map(row -> MediaType.valueOf(row)).toArray(size -> new MediaType[size]);
            return this;
        }

        /**
         * Sets the {@code Content-Type} header for this client.
         *
         * @param type The content type.
         * @return Returns the builder, for chaining.
         */
        public WebClientBuilder type(String type)
        {
            this.type = MediaType.valueOf(type);
            return this;
        }

        /**
         * Enables chunking.
         *
         * @param threshold The threshold for chunks.
         * @return Returns the builder, for chaining.
         */
        public WebClientBuilder allowChunking(int threshold)
        {
            this.allowChunking = true;
            this.chunkingThreshold = threshold;
            return this;
        }

        /**
         * Indicates that outgoing contents should be stored on the CXF {@link Message messages}.
         * <p>
         * The outgoing body can be fetched through the {@link Message} by calling:
         *
         * <pre>
         * String body = (String) message.get(WebClientFactory.OUTGOING_BODY_KEY);
         * </pre>
         * </p>
         * <p>
         * Note that the value can be null, for example in {@code GET} requests.
         * </p>
         *
         * @return Returns the builder, for chaining.
         */
        public WebClientBuilder storeOutgoingContents()
        {
            bean.getOutInterceptors().add(new WriteBodyToMessageOutInterceptor());
            return this;
        }

        /**
         * Adds an outgoing interceptor for the client.
         *
         * @param interceptor The interceptor to add.
         * @return Returns the builder, for chaining.
         */
        public WebClientBuilder outInterceptor(Interceptor<? extends Message> interceptor)
        {
            bean.getOutInterceptors().add(interceptor);
            return this;
        }

        /**
         * Indicates that this client should use JSON serialization and deserialization using Jackson.
         *
         * @return Returns the builder, for chaining.
         */
        public WebClientBuilder serializationWithJson()
        {
            AnnotationIntrospector pair = AnnotationIntrospector.pair(
                    new JacksonAnnotationIntrospector(),
                    new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()));

            ObjectMapper mapper = new ObjectMapper();
            mapper.setAnnotationIntrospector(pair);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            return serializationWithJson(mapper);
        }

        /**
         * Indicates that this client should use JSON serialization and deserialization using Jackson.
         *
         * @param mapper The mapper to use.
         * @return Returns the builder, for chaining.
         */
        public WebClientBuilder serializationWithJson(ObjectMapper mapper)
        {
            JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
            provider.setMapper(mapper);
            providers.add(provider);
            return this;
        }

        /**
         * Builds the client.
         *
         * @return Returns the created client.
         */
        public WebClient build()
        {
            if (!providers.isEmpty())
            {
                bean.setProviders(providers);
            }

            WebClient client = bean.createWebClient();
            if (accepts != null)
            {
                client.accept(accepts);
            }
            if (type != null)
            {
                client.type(type);
            }

            HTTPClientPolicy clientPolicy = new HTTPClientPolicy();
            clientPolicy.setAllowChunking(allowChunking);
            clientPolicy.setChunkingThreshold(chunkingThreshold);

            ClientConfiguration config = WebClient.getConfig(client);
            HTTPConduit conduit = config.getHttpConduit();
            conduit.setClient(clientPolicy);

            return client;
        }
    }
}
