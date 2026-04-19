package com.listatarefas.dto;

import com.listatarefas.domain.StatusTarefa;
import com.listatarefas.domain.Tarefa;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TarefaResponse {

    Long id;
    String nome;
    String descricao;
    StatusTarefa status;
    String observacoes;
    Instant dataCriacao;
    Instant dataAtualizacao;

    public static TarefaResponse fromEntity(Tarefa t) {
        return TarefaResponse.builder()
                .id(t.getId())
                .nome(t.getNome())
                .descricao(t.getDescricao())
                .status(t.getStatus())
                .observacoes(t.getObservacoes())
                .dataCriacao(t.getDataCriacao())
                .dataAtualizacao(t.getDataAtualizacao())
                .build();
    }
}
