package SIGA_IPT.backend.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

public abstract class ControllerTestBase {

    @Autowired
    protected MockMvc mockMvc;

    protected static RequestPostProcessor admin(){
        return userWithRole("ADMIN", "admin@siga.cl");
    }

    protected static RequestPostProcessor teacher(){
        return userWithRole("TEACHER", "teacher@siga.cl");
    }

    protected static RequestPostProcessor student(){
        return userWithRole("STUDENT", "student@siga.cl");
    }

    private static RequestPostProcessor userWithRole(String role, String email){
        return jwt()
                .jwt(token -> token.subject("kc-" + role.toLowerCase()).claim("email", email).claim("preferred_username", role.toLowerCase()))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    protected ResultActions call(HttpMethod method, String url, RequestPostProcessor auth, String json) throws Exception {
        MockHttpServletRequestBuilder builder = request(method, url);
        if (auth != null){
            builder.with(auth);
        }
        if (json != null){
            builder.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return mockMvc.perform(builder);
    }

    protected ResultActions get(String url, RequestPostProcessor auth) throws Exception {
        return call(HttpMethod.GET, url, auth, null);
    }

    protected ResultActions post(String url, RequestPostProcessor auth, String json) throws Exception {
        return call(HttpMethod.POST, url, auth, json);
    }

    protected ResultActions put(String url, RequestPostProcessor auth, String json) throws Exception {
        return call(HttpMethod.PUT, url, auth, json);
    }

    protected ResultActions delete(String url, RequestPostProcessor auth) throws Exception {
        return call(HttpMethod.DELETE, url, auth, null);
    }
}
