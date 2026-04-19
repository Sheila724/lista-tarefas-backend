package com.listatarefas.dto;

import com.listatarefas.domain.StatusTarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarefaRequest {

    @NotBlank
    @Size(max = 200)
    private String nome;

    private String descricao;

    @NotNull
    private StatusTarefa status;

    private String observacoes;
}
