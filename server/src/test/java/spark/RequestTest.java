package spark;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import spark.routematch.RouteMatch;
import spark.util.SparkTestUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static spark.Spark.after;
import static spark.Spark.afterAfter;
import static spark.Spark.awaitInitialization;
import static spark.Spark.before;
import static spark.Spark.get;

public class RequestTest extends SparkBaseTest {

    private static final String THE_SERVLET_PATH = "/the/servlet/path";
    private static final String THE_CONTEXT_PATH = "/the/context/path";
    private static final String THE_MATCHED_ROUTE = "/users/:username";
    private static final String BEFORE_MATCHED_ROUTE = "/users/:before";
    private static final String AFTER_MATCHED_ROUTE = "/users/:after";
    private static final String AFTERAFTER_MATCHED_ROUTE = "/users/:afterafter";

    private static SparkTestUtil http;

    HttpServletRequest servletRequest;
    HttpSession httpSession;
    Request request;

    RouteMatch match = new RouteMatch(null, "/hi", "/hi", "text/html", null);
    RouteMatch matchWithParams = new RouteMatch(null, "/users/:username", "/users/bob", "text/html", null);

    @Before
    public void setup() {
        http = new SparkTestUtil(4567);

        before(BEFORE_MATCHED_ROUTE, (q, a) -> {
            System.out.println("before filter matched");
            shouldBeAbleToGetTheMatchedPathInBeforeFilter(q);
        });
        get(THE_MATCHED_ROUTE, (q, a) -> "Get filter matched");
        after(AFTER_MATCHED_ROUTE, (q, a) -> {
            System.out.println("after filter matched");
            shouldBeAbleToGetTheMatchedPathInAfterFilter(q);
        });
        afterAfter(AFTERAFTER_MATCHED_ROUTE, (q, a) -> {
            System.out.println("afterafter filter matched");
            shouldBeAbleToGetTheMatchedPathInAfterAfterFilter(q);
        });

        awaitInitialization();

        servletRequest = mock(HttpServletRequest.class);
        httpSession = mock(HttpSession.class);

        request = new Request(match, servletRequest);

    }

    @Test
    public void queryParamShouldReturnsParametersFromQueryString() {

        when(servletRequest.getParameter("name")).thenReturn("Federico");

        String name = request.queryParams("name");
        Assert.assertEquals("Invalid name in query string", "Federico", name);
    }

    @Test
    public void queryParamOrDefault_shouldReturnQueryParam_whenQueryParamExists() {

        when(servletRequest.getParameter("name")).thenReturn("Federico");

        String name = request.queryParamOrDefault("name", "David");
        Assert.assertEquals("Invalid name in query string", "Federico", name);
    }

    @Test
    public void queryParamOrDefault_shouldReturnDefault_whenQueryParamIsNull() {

        when(servletRequest.getParameter("name")).thenReturn(null);

        String name = request.queryParamOrDefault("name", "David");
        Assert.assertEquals("Invalid name in default value", "David", name);
    }

    @Test
    public void queryParamShouldBeParsedAsHashMap() {
        Map<String, String[]> params = new HashMap<>();
        params.put("user[name]", new String[]{"Federico"});

        when(servletRequest.getParameterMap()).thenReturn(params);

        String name = request.queryMap("user").value("name");
        Assert.assertEquals("Invalid name in query string", "Federico", name);
    }

    @Test
    public void shouldBeAbleToGetTheServletPath() {

        when(servletRequest.getServletPath()).thenReturn(THE_SERVLET_PATH);

        Request request = new Request(match, servletRequest);
        assertEquals("Should have delegated getting the servlet path", THE_SERVLET_PATH, request.servletPath());
    }

    @Test
    public void shouldBeAbleToGetTheContextPath() {

        when(servletRequest.getContextPath()).thenReturn(THE_CONTEXT_PATH);

        Request request = new Request(match, servletRequest);
        assertEquals("Should have delegated getting the context path", THE_CONTEXT_PATH, request.contextPath());
    }

    @Test
    public void shouldBeAbleToGetTheMatchedPath() {
        Request request = new Request(matchWithParams, servletRequest);
        assertEquals("Should have returned the matched route", THE_MATCHED_ROUTE, request.matchedPath());
        try {
            http.get("/users/bob");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shouldBeAbleToGetTheMatchedPathInBeforeFilter(Request q) {
        assertEquals("Should have returned the matched route from the before filter", BEFORE_MATCHED_ROUTE, q.matchedPath());
    }

    public void shouldBeAbleToGetTheMatchedPathInAfterFilter(Request q) {
        assertEquals("Should have returned the matched route from the after filter", AFTER_MATCHED_ROUTE, q.matchedPath());
    }

    public void shouldBeAbleToGetTheMatchedPathInAfterAfterFilter(Request q) {
        assertEquals("Should have returned the matched route from the afterafter filter", AFTERAFTER_MATCHED_ROUTE, q.matchedPath());
    }

    @Test
    public void testSessionNoParams_whenSessionIsNull() {

        when(servletRequest.getSession()).thenReturn(httpSession);

        assertEquals("A Session with an HTTPSession from the Request should have been created",
            httpSession, request.session().raw());
    }

    @Test
    public void testSession_whenCreateIsTrue() {

        when(servletRequest.getSession(true)).thenReturn(httpSession);

        assertEquals("A Session with an HTTPSession from the Request should have been created because create parameter " +
                "was set to true",
            httpSession, request.session(true).raw());

    }

    @Test
    public void testSession_whenCreateIsFalse() {

        when(servletRequest.getSession(true)).thenReturn(httpSession);

        assertEquals("A Session should not have been created because create parameter was set to false",
            null, request.session(false));

    }

    @Test
    public void testSessionNpParams_afterSessionInvalidate() {
        when(servletRequest.getSession()).thenReturn(httpSession);

        Session session = request.session();
        session.invalidate();
        request.session();

        verify(servletRequest, times(2)).getSession();
    }

    @Test
    public void testSession_whenCreateIsTrue_afterSessionInvalidate() {
        when(servletRequest.getSession(true)).thenReturn(httpSession);

        Session session = request.session(true);
        session.invalidate();
        request.session(true);

        verify(servletRequest, times(2)).getSession(true);
    }

    @Test
    public void testSession_whenCreateIsFalse_afterSessionInvalidate() {
        when(servletRequest.getSession()).thenReturn(httpSession);
        when(servletRequest.getSession(false)).thenReturn(null);

        Session session = request.session();
        session.invalidate();
        request.session(false);

        verify(servletRequest, times(1)).getSession(false);
    }

    @Test
    public void testSession_2times() {
        when(servletRequest.getSession(true)).thenReturn(httpSession);

        Session session = request.session(true);
        session = request.session(true);

        Assert.assertNotNull(session);
        verify(servletRequest, times(1)).getSession(true);
    }

    @Test
    public void testCookies_whenCookiesArePresent() {

        Collection<Cookie> cookies = new ArrayList<>();
        cookies.add(new Cookie("cookie1", "cookie1value"));
        cookies.add(new Cookie("cookie2", "cookie2value"));

        Map<String, String> expected = new HashMap<>();
        for (Cookie cookie : cookies) {
            expected.put(cookie.getName(), cookie.getValue());
        }

        Cookie[] cookieArray = cookies.toArray(new Cookie[cookies.size()]);

        when(servletRequest.getCookies()).thenReturn(cookieArray);

        Assert.assertTrue("The count of cookies returned should be the same as those in the request",
            request.cookies().size() == 2);

        assertEquals("A Map of Cookies should have been returned because they exist", expected, request.cookies());

    }

    @Test
    public void testCookies_whenCookiesAreNotPresent() {

        when(servletRequest.getCookies()).thenReturn(null);

        Assert.assertNotNull("A Map of Cookies should have been instantiated even if cookies are not present in the request",
            request.cookies());

        Assert.assertTrue("The Map of cookies should be empty because cookies are not present in the request",
            request.cookies().size() == 0);

    }

    @Test
    public void testCookie_whenCookiesArePresent() {

        final String cookieKey = "cookie1";
        final String cookieValue = "cookie1value";

        Collection<Cookie> cookies = new ArrayList<>();
        cookies.add(new Cookie(cookieKey, cookieValue));

        Cookie[] cookieArray = cookies.toArray(new Cookie[cookies.size()]);
        when(servletRequest.getCookies()).thenReturn(cookieArray);

        Assert.assertNotNull("A value for the key provided should exist because a cookie with the same key is present",
            request.cookie(cookieKey));

        assertEquals("The correct value for the cookie key supplied should be returned",
            cookieValue, request.cookie(cookieKey));

    }

    @Test
    public void testCookie_whenCookiesAreNotPresent() {

        final String cookieKey = "nonExistentCookie";

        when(servletRequest.getCookies()).thenReturn(null);

        Assert.assertNull("A null value should have been returned because the cookie with that key does not exist",
            request.cookie(cookieKey));

    }

    @Test
    public void testRequestMethod() {

        final String requestMethod = "GET";

        when(servletRequest.getMethod()).thenReturn(requestMethod);

        assertEquals("The request method of the underlying servlet request should be returned",
            requestMethod, request.requestMethod());

    }

    @Test
    public void testScheme() {

        final String scheme = "http";

        when(servletRequest.getScheme()).thenReturn(scheme);

        assertEquals("The scheme of the underlying servlet request should be returned",
            scheme, request.scheme());

    }

    @Test
    public void testHost() {

        final String host = "www.google.com";

        when(servletRequest.getHeader("host")).thenReturn(host);

        assertEquals("The value of the host header of the underlying servlet request should be returned",
            host, request.host());

    }

    @Test
    public void testUserAgent() {

        final String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36";

        when(servletRequest.getHeader("user-agent")).thenReturn(userAgent);

        assertEquals("The value of the user agent header of the underlying servlet request should be returned",
            userAgent, request.userAgent());

    }

    @Test
    public void testPort() {

        final int port = 80;

        when(servletRequest.getServerPort()).thenReturn(80);

        assertEquals("The server port of the the underlying servlet request should be returned",
            port, request.port());

    }

    @Test
    public void testPathInfo() {

        final String pathInfo = "/path/to/resource";

        when(servletRequest.getPathInfo()).thenReturn(pathInfo);

        assertEquals("The path info of the underlying servlet request should be returned",
            pathInfo, request.pathInfo());

    }

    @Test
    public void testServletPath() {

        final String servletPath = "/api";

        when(servletRequest.getServletPath()).thenReturn(servletPath);

        assertEquals("The servlet path of the underlying servlet request should be returned",
            servletPath, request.servletPath());

    }

    @Test
    public void testContextPath() {

        final String contextPath = "/my-app";

        when(servletRequest.getContextPath()).thenReturn(contextPath);

        assertEquals("The context path of the underlying servlet request should be returned",
            contextPath, request.contextPath());

    }

    @Test
    public void testUrl() {

        final String url = "http://www.myapp.com/myapp/a";

        when(servletRequest.getRequestURL()).thenReturn(new StringBuffer(url));

        assertEquals("The request url of the underlying servlet request should be returned",
            url, request.url());

    }

    @Test
    public void testContentType() {

        final String contentType = "image/jpeg";

        when(servletRequest.getContentType()).thenReturn(contentType);

        assertEquals("The content type of the underlying servlet request should be returned",
            contentType, request.contentType());

    }

    @Test
    public void testIp() {

        final String ip = "216.58.197.106:80";

        when(servletRequest.getRemoteAddr()).thenReturn(ip);

        assertEquals("The remote IP of the underlying servlet request should be returned",
            ip, request.ip());

    }

    @Test
    public void testContentLength() {

        final int contentLength = 500;

        when(servletRequest.getContentLength()).thenReturn(contentLength);

        assertEquals("The content length the underlying servlet request should be returned",
            contentLength, request.contentLength());

    }

    @Test
    public void testHeaders() {

        final String headerKey = "host";
        final String host = "www.google.com";

        when(servletRequest.getHeader(headerKey)).thenReturn(host);

        assertEquals("The value of the header specified should be returned",
            host, request.headers(headerKey));

    }

    @Test
    public void testQueryParamsValues_whenParamExists() {

        final String[] paramValues = {"foo", "bar"};

        when(servletRequest.getParameterValues("id")).thenReturn(paramValues);

        assertArrayEquals("An array of Strings for a parameter with multiple values should be returned",
            paramValues, request.queryParamsValues("id"));

    }

    @Test
    public void testQueryParamsValues_whenParamDoesNotExists() {

        when(servletRequest.getParameterValues("id")).thenReturn(null);

        Assert.assertNull("Null should be returned because the parameter specified does not exist in the request",
            request.queryParamsValues("id"));

    }

    @Test
    public void testQueryParams() {

        Map<String, String[]> params = new HashMap<>();
        params.put("sort", new String[]{"asc"});
        params.put("items", new String[]{"10"});

        when(servletRequest.getParameterMap()).thenReturn(params);

        Set<String> result = request.queryParams();

        Assert.assertArrayEquals("Should return the query parameter names", params.keySet().toArray(), result.toArray());

    }

    @Test
    public void testURI() {

        final String requestURI = "http://localhost:8080/myapp/";

        when(servletRequest.getRequestURI()).thenReturn(requestURI);

        assertEquals("The request URI should be returned",
            requestURI, request.uri());

    }

    @Test
    public void testProtocol() {

        final String protocol = "HTTP/1.1";

        when(servletRequest.getProtocol()).thenReturn(protocol);

        assertEquals("The underlying request protocol should be returned",
            protocol, request.protocol());

    }
}
