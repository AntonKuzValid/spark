/*
 * Copyright 2011- Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spark;

import javax.servlet.http.HttpServletResponse;

/**
 * Exception used for stopping the execution
 *
 * @author Per Wendel
 */
public class HaltException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int statusCode = HttpServletResponse.SC_OK;
    private String body = null;

    HaltException() {
        super(null, null, false, false);
    }

    HaltException(int statusCode) {
        this();
        this.statusCode = statusCode;
    }

    HaltException(String body) {
        this();
        this.body = body;
    }

    HaltException(int statusCode, String body) {
        this();
        this.statusCode = statusCode;
        this.body = body;
    }

    /**
     * @return the statusCode
     * @deprecated replaced by {@link #statusCode()}
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @return the statusCode
     */
    public int statusCode() {
        return statusCode;
    }

    /**
     * @return the body
     * @deprecated replaced by {@link #body()}
     */
    public String getBody() {
        return body;
    }

    /**
     * @return the body
     */
    public String body() {
        return body;
    }

}
