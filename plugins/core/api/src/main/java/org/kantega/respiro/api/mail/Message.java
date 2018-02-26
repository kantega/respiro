/*
 * Copyright 2015 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kantega.respiro.api.mail;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Message {

    public static final String HTML_MESSAGE_BEGIN = "<html><body>";
    public static final String HTML_MESSAGE_END = "</html></body>";

    /**
     * Override if this message is from a different address.
     */
    private String from = null; 
    private final List<String> to = new ArrayList<>();
    private final List<String> cc = new ArrayList<>();
    private final List<String> bcc = new ArrayList<>();
    private final List<Attachment> attachments = new ArrayList<>();

    private final StringBuilder body = new StringBuilder();
    private final String subject;
    private Charset charset = Charset.defaultCharset();

    private StringBuilder plainTextBody;
    private boolean html = false;

    public Message(String subject) {
        this.subject = subject;
    }

    public Message to(String address) {
        to.add(address);
        return this;
    }

    public Message cc(String address) {
        cc.add(address);
        return this;
    }

    public Message bcc(String address) {
        bcc.add(address);
        return this;
    }
    
    public Message from(String from) {
        this.from = from;
        return this;
    }

    /**
     * NOTE: If the {@code html} flag is set html and body-tags might be omitted, since{@link #getHtmlBody()} adds
     * these in if the message does not start with {@code "<html>"} or {@code "<!DOCTYPE"}
     * <p>
     * This is for convenient usage of adding html parts, making this method more intuitive in use
     * and correctly named.
     */
    public Message body(String bodyPart) {
        body.append(bodyPart);
        return this;
    }

    public Message charset(Charset charset){
        this.charset = charset;
        return this;
    }

    public Message charset(String charset) {
        return charset(Charset.forName(charset));
    }

    public Message attach(String filename, String mimeType, byte[] content) {
        attachments.add(new Attachment(mimeType, filename, content));
        return this;
    }

    public Message html(boolean html) {
        this.html = html;
        return this;
    }

    /**
     * Only relevant if html is set to {@code true}, the plain text body
     * of the message is used as the alternative text content for clients
     * not supporting the HTML mime type.
     * <p>
     * If unset the regular body will be used.
     */
    public Message plainTextBody(String bodyPart) {
        if (plainTextBody == null) {
            plainTextBody = new StringBuilder();
        }
        plainTextBody.append(bodyPart);
        return this;
    }

    public String getFrom() {
        return from;
    }

    public List<String> getTo() {
        return to;
    }

    public List<String> getCc() {
        return cc;
    }

    public List<String> getBcc() {
        return bcc;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body.toString();
    }

    /**
     * If the {@code html} flag is set and the concatenated body
     * parts are not recognized as html (not starting with "{@code <html>}" or "{@code <!DOCTYPE}"),
     * this method will return the concatenated body parts wrapped in html and body tags:
     * <p>
     * {@code
     * <html><body>${the concatenated body parts}</body></html>
     * }
     *
     *
     * If the {@code html} flag is not set this method returns the same as {@link #getBody()}
     */
    public String getHtmlBody() {
        String str = getBody();
        if (isHtml() && !str.startsWith("<html>") && !str.startsWith("<!DOCTYPE")) {
            str = HTML_MESSAGE_BEGIN + str + HTML_MESSAGE_END;
        }
        return str;
    }

    public Charset getCharset() {
        return charset;
    }

    public boolean isHtml() {
        return html;
    }

    public String getPlainTextBody() {
        if (plainTextBody != null) {
            return plainTextBody.toString();
        }

        return "";
    }
}
