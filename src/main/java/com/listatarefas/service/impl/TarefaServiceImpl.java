package com.listatarefas.service.impl;

import com.listatarefas.domain.Tarefa;
import com.listatarefas.dto.TarefaRequest;
import com.listatarefas.dto.TarefaResponse;
import com.listatarefas.exception.RecursoNaoEncontradoException;
import com.listatarefas.repository.TarefaRepository;
import com.listatarefas.service.TarefaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TarefaServiceImpl implements TarefaService {

    private final TarefaRepository tarefaRepository;

    @Override
    @Transactional
    public TarefaResponse criar(TarefaRequest request) {
        Tarefa entity = Tarefa.builder()
                .nome(request.getNome())
                .descricao(request.getDescricao())
                .status(request.getStatus())
                .observacoes(request.getObservacoes())
                .build();
        Tarefa salva = tarefaRepository.save(entity);
        return TarefaResponse.fromEntity(salva);
    }

    @Override
    @Transactional
    public TarefaResponse atualizar(Long id, TarefaRequest request) {
        Tarefa entity = tarefaRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Tarefa não encontrada: " + id));
        entity.setNome(request.getNome());
        entity.setDescricao(request.getDescricao());
        entity.setStatus(request.getStatus());
        entity.setObservacoes(request.getObservacoes());
        tarefaRepository.save(entity);
        return TarefaResponse.fromEntity(entity);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        if (!tarefaRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Tarefa não encontrada: " + id);
        }
        tarefaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public TarefaResponse buscarPorId(Long id) {
        Tarefa entity = tarefaRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Tarefa não encontrada: " + id));
        return TarefaResponse.fromEntity(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TarefaResponse> listarTodas() {
        return tarefaRepository.findAll().stream().map(TarefaResponse::fromEntity).toList();
    }
}
