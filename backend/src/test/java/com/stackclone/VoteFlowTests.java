package com.stackclone;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VoteFlowTests extends ApiTestSupport {

    @Test
    void votesAdjustScoreAndReputation() throws Exception {
        String asker = registerUser("voteasker");
        String answerer = registerUser("voteanswerer");
        Long askerId = userId(asker);
        Long answererId = userId(answerer);

        Long questionId = idOf(call(POST, "/api/questions", asker, """
                {"title":"Pourquoi mon vote ne compte pas ?","body":"Une question de test pour vérifier les votes et la réputation.","tags":["meta"]}"""));
        Long answerId = idOf(call(POST, "/api/questions/" + questionId + "/answers", answerer,
                "{\"body\":\"Parce qu'il faut 15 points de réputation pour voter.\"}"));

        // Nouveau compte (1 point) : pas encore le droit de voter
        call(POST, "/api/posts/" + questionId + "/vote", answerer, "{\"value\":1}").andExpect(status().isForbidden());

        // Réponse acceptée : +15 pour l'auteur de la réponse, +2 pour celui de la question
        call(POST, "/api/answers/" + answerId + "/accept", asker, null).andExpect(status().isOk());
        reputation(answererId, 16);
        reputation(askerId, 3);

        // Maintenant l'upvote est autorisé : +10 pour l'auteur de la question
        call(POST, "/api/posts/" + questionId + "/vote", answerer, "{\"value\":1}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(1))
                .andExpect(jsonPath("$.myVote").value(1));
        reputation(askerId, 13);
        call(GET, "/api/questions/" + questionId, answerer, null).andExpect(jsonPath("$.myVote").value(1));

        // Annuler le vote remet tout à zéro
        call(POST, "/api/posts/" + questionId + "/vote", answerer, "{\"value\":0}")
                .andExpect(jsonPath("$.score").value(0));
        reputation(askerId, 3);

        // Le downvote demande 125 points, et on ne vote jamais pour soi-même
        call(POST, "/api/posts/" + questionId + "/vote", answerer, "{\"value\":-1}").andExpect(status().isForbidden());
        call(POST, "/api/posts/" + answerId + "/vote", answerer, "{\"value\":1}").andExpect(status().isForbidden());
        call(POST, "/api/posts/" + questionId + "/vote", answerer, "{\"value\":5}").andExpect(status().isBadRequest());

        // Le tag apparaît dans la liste des tags
        call(GET, "/api/tags?q=meta", null, null).andExpect(jsonPath("$.content[*].name", hasItem("meta")));
    }

    private Long userId(String token) throws Exception {
        String body = call(GET, "/api/auth/me", token, null).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private void reputation(Long userId, int expected) throws Exception {
        call(GET, "/api/users/" + userId, null, null).andExpect(jsonPath("$.reputation").value(expected));
    }
}
