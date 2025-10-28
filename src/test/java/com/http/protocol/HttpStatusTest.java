package com.http.protocol;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HttpStatusTest {

    @Test
    void testStatusCodeValues() {
        assertEquals(200, HttpStatus.OK.getCode());
        assertEquals(301, HttpStatus.MOVED_PERMANENTLY.getCode());
        assertEquals(302, HttpStatus.FOUND.getCode());
        assertEquals(304, HttpStatus.NOT_MODIFIED.getCode());
        assertEquals(400, HttpStatus.BAD_REQUEST.getCode());
        assertEquals(401, HttpStatus.UNAUTHORIZED.getCode());
        assertEquals(404, HttpStatus.NOT_FOUND.getCode());
        assertEquals(405, HttpStatus.METHOD_NOT_ALLOWED.getCode());
        assertEquals(500, HttpStatus.INTERNAL_SERVER_ERROR.getCode());
        assertEquals(503, HttpStatus.SERVICE_UNAVAILABLE.getCode());
    }

    @Test
    void testStatusMessages() {
        assertEquals("OK", HttpStatus.OK.getMessage());
        assertEquals("Not Found", HttpStatus.NOT_FOUND.getMessage());
        assertEquals("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.getMessage());
    }

    @Test
    void testFromCode() {
        assertEquals(HttpStatus.OK, HttpStatus.fromCode(200));
        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.fromCode(404));
        assertNull(HttpStatus.fromCode(999));
    }

    @Test
    void testToString() {
        assertEquals("200 OK", HttpStatus.OK.toString());
        assertEquals("404 Not Found", HttpStatus.NOT_FOUND.toString());
    }
}
