package com.stackclone;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class QuestionFlowTests extends ApiTestSupport {

    private static final String QUESTION = """
            {"title":"Comment inverser une liste en Java ?",
             "body":"J'ai une List<Integer> et je voudrais l'inverser sans créer de copie. Comment faire ?",
             "tags":["Java","collections"]}""";

    @Test
    void fullQuestionLifecycle() throws Exception {
        String asker = registerUser("asker");
        String answerer = registerUser("answerer");

        // Création : les tags sont normalisés en minuscules
        Long questionId = idOf(call(POST, "/api/questions", asker, QUESTION)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tags[0]").value("java")));

        // Liste publique, filtre par tag et recherche plein texte
        call(GET, "/api/questions?tag=java", null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(questionId.intValue())));
        call(GET, "/api/questions?q=inverser liste", null, null)
                .andExpect(jsonPath("$.content[*].id", hasItem(questionId.intValue())));

        // Un autre utilisateur ne peut pas modifier la question
        call(PUT, "/api/questions/" + questionId, answerer, QUESTION).andExpect(status().isForbidden());

        // Réponse + commentaire
        Long answerId = idOf(call(POST, "/api/questions/" + questionId + "/answers", answerer,
                "{\"body\":\"Utilise Collections.reverse(liste), ça inverse sur place.\"}")
                .andExpect(status().isCreated()));
        call(POST, "/api/posts/" + answerId + "/comments", asker, "{\"body\":\"Merci, parfait !\"}")
                .andExpect(status().isCreated());

        // Seul l'auteur de la question peut accepter
        call(POST, "/api/answers/" + answerId + "/accept", answerer, null).andExpect(status().isForbidden());
        call(POST, "/api/answers/" + answerId + "/accept", asker, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(true));

        call(GET, "/api/questions/" + questionId, null, null)
                .andExpect(jsonPath("$.acceptedAnswerId").value(answerId))
                .andExpect(jsonPath("$.answers[0].comments[0].body").value("Merci, parfait !"))
                .andExpect(jsonPath("$.viewCount").value(1));

        // L'écriture sans token est refusée
        call(POST, "/api/questions", null, QUESTION).andExpect(status().isUnauthorized());

        // Suppression
        call(DELETE, "/api/questions/" + questionId, asker, null).andExpect(status().isNoContent());
        call(GET, "/api/questions/" + questionId, null, null).andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidQuestion() throws Exception {
        String token = registerUser("bad");
        call(POST, "/api/questions", token, "{\"title\":\"court\",\"body\":\"court\",\"tags\":[]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.tags").exists());
        call(POST, "/api/questions", token, "{pas du json")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Corps de requête JSON invalide"));
    }
}
