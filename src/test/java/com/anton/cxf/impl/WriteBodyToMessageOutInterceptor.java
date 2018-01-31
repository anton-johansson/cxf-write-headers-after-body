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
import static org.apache.cxf.phase.Phase.PRE_STREAM;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

/**
 * An outgoing interceptor that puts the final body to the message.
 */
class WriteBodyToMessageOutInterceptor extends AbstractPhaseInterceptor<Message>
{
    WriteBodyToMessageOutInterceptor()
    {
        super(PRE_STREAM);
    }

    @Override
    public void handleMessage(Message message) throws Fault
    {
        OutputStream content = message.getContent(OutputStream.class);
        if (content != null)
        {
            content = new FlushToMessageOutputStream(message, content);
            message.setContent(OutputStream.class, content);
        }
    }

    /**
     * Defines the cached output stream.
     */
    static class FlushToMessageOutputStream extends OutputStream
    {
        private final StringBuilder data = new StringBuilder();
        private final Message message;
        private final OutputStream wrapee;

        private FlushToMessageOutputStream(Message message, OutputStream wrapee)
        {
            this.message = message;
            this.wrapee = wrapee;
        }

        @Override
        public void write(int b) throws IOException
        {
            data.append((char) b);
            wrapee.write(b);
        }

        @Override
        public void flush() throws IOException
        {
            message.put(OUTGOING_BODY_KEY, data.toString());
            super.flush();
        }

        @Override
        public void close() throws IOException
        {
            wrapee.close();
        }
    }
}
