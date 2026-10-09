package com.stackclone;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

/** Petits helpers pour écrire des tests d'API lisibles. */
abstract class ApiTestSupport {

    @Autowired
    MockMvc mvc;

    /** Crée un utilisateur unique et renvoie son token JWT. */
    String registerUser(String prefix) throws Exception {
        String name = prefix + UUID.randomUUID().toString().substring(0, 8);
        String body = call(HttpMethod.POST, "/api/auth/register", null,
                "{\"username\":\"%s\",\"email\":\"%s@test.dev\",\"password\":\"motdepasse\"}".formatted(name, name))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    ResultActions call(HttpMethod method, String url, String token, String json) throws Exception {
        MockHttpServletRequestBuilder req = request(method, url);
        if (token != null) {
            req.header("Authorization", "Bearer " + token);
        }
        if (json != null) {
            req.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return mvc.perform(req);
    }

    static Long idOf(ResultActions result) throws Exception {
        return ((Number) JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id")).longValue();
    }
}
