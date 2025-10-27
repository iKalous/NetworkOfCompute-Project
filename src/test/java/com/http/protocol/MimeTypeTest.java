package com.http.protocol;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MimeTypeTest {

    @Test
    void testHtmlMimeType() {
        assertEquals("text/html", MimeType.getByExtension("index.html"));
        assertEquals("text/html", MimeType.getByExtension("page.HTML"));
    }

    @Test
    void testTextMimeType() {
        assertEquals("text/plain", MimeType.getByExtension("file.txt"));
        assertEquals("text/plain", MimeType.getByExtension("readme.TXT"));
    }

    @Test
    void testJsonMimeType() {
        assertEquals("application/json", MimeType.getByExtension("data.json"));
        assertEquals("application/json", MimeType.getByExtension("config.JSON"));
    }

    @Test
    void testPngMimeType() {
        assertEquals("image/png", MimeType.getByExtension("logo.png"));
        assertEquals("image/png", MimeType.getByExtension("image.PNG"));
    }

    @Test
    void testUnknownExtension() {
        assertEquals("application/octet-stream", MimeType.getByExtension("file.xyz"));
        assertEquals("application/octet-stream", MimeType.getByExtension("document.pdf"));
    }

    @Test
    void testNoExtension() {
        assertEquals("application/octet-stream", MimeType.getByExtension("filename"));
        assertEquals("application/octet-stream", MimeType.getByExtension(""));
        assertEquals("application/octet-stream", MimeType.getByExtension(null));
    }

    @Test
    void testDefaultMimeType() {
        assertEquals("application/octet-stream", MimeType.getDefault());
    }
}
