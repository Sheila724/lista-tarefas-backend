package com.listatarefas.service;

import com.listatarefas.dto.TarefaRequest;
import com.listatarefas.dto.TarefaResponse;
import java.util.List;

public interface TarefaService {

    TarefaResponse criar(TarefaRequest request);

    TarefaResponse atualizar(Long id, TarefaRequest request);

    void excluir(Long id);

    TarefaResponse buscarPorId(Long id);

    List<TarefaResponse> listarTodas();
}
