package com.listatarefas.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.listatarefas.domain.StatusTarefa;
import com.listatarefas.dto.TarefaRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TarefaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fluxoCrud_basico() throws Exception {
        TarefaRequest criar = TarefaRequest.builder()
                .nome("Comprar leite")
                .descricao("No mercado")
                .status(StatusTarefa.PENDENTE)
                .observacoes("Integral")
                .build();

        String jsonCriado = mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criar)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Comprar leite"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long id = objectMapper.readTree(jsonCriado).get("id").asLong();

        mockMvc.perform(get("/api/tarefas/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        TarefaRequest atualizar = TarefaRequest.builder()
                .nome("Comprar leite")
                .descricao("No mercado")
                .status(StatusTarefa.CONCLUIDA)
                .observacoes("Feito")
                .build();

        mockMvc.perform(put("/api/tarefas/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizar)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));

        mockMvc.perform(get("/api/tarefas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(delete("/api/tarefas/" + id)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tarefas/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void criar_comNomeVazio_deve400() throws Exception {
        TarefaRequest invalido = TarefaRequest.builder()
                .nome("")
                .status(StatusTarefa.PENDENTE)
                .build();

        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }
}
