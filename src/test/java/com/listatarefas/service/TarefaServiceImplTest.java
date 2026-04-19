package com.listatarefas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listatarefas.domain.StatusTarefa;
import com.listatarefas.domain.Tarefa;
import com.listatarefas.dto.TarefaRequest;
import com.listatarefas.dto.TarefaResponse;
import com.listatarefas.exception.RecursoNaoEncontradoException;
import com.listatarefas.repository.TarefaRepository;
import com.listatarefas.service.impl.TarefaServiceImpl;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TarefaServiceImplTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @InjectMocks
    private TarefaServiceImpl tarefaService;

    private Tarefa tarefaPersistida;

    @BeforeEach
    void setUp() {
        Instant agora = Instant.parse("2026-04-19T12:00:00Z");
        tarefaPersistida = Tarefa.builder()
                .id(1L)
                .nome("Estudar Spring")
                .descricao("Capítulo JPA")
                .status(StatusTarefa.PENDENTE)
                .observacoes("Usar IntelliJ")
                .dataCriacao(agora)
                .dataAtualizacao(agora)
                .build();
    }

    @Test
    void criar_devePersistirERetornarDto() {
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(invocation -> {
            Tarefa t = invocation.getArgument(0);
            t.setId(99L);
            t.setDataCriacao(Instant.parse("2026-04-19T12:00:00Z"));
            t.setDataAtualizacao(Instant.parse("2026-04-19T12:00:00Z"));
            return t;
        });

        TarefaRequest req = TarefaRequest.builder()
                .nome("Nova")
                .descricao("D")
                .status(StatusTarefa.EM_ANDAMENTO)
                .observacoes("obs")
                .build();

        TarefaResponse resp = tarefaService.criar(req);

        assertThat(resp.getId()).isEqualTo(99L);
        assertThat(resp.getNome()).isEqualTo("Nova");
        assertThat(resp.getStatus()).isEqualTo(StatusTarefa.EM_ANDAMENTO);
        verify(tarefaRepository).save(any(Tarefa.class));
    }

    @Test
    void atualizar_quandoExiste_deveAtualizarCampos() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefaPersistida));

        TarefaRequest req = TarefaRequest.builder()
                .nome("Atualizado")
                .descricao("Nova desc")
                .status(StatusTarefa.CONCLUIDA)
                .observacoes(null)
                .build();

        TarefaResponse resp = tarefaService.atualizar(1L, req);

        assertThat(resp.getNome()).isEqualTo("Atualizado");
        assertThat(resp.getStatus()).isEqualTo(StatusTarefa.CONCLUIDA);
        assertThat(tarefaPersistida.getDescricao()).isEqualTo("Nova desc");
    }

    @Test
    void atualizar_quandoNaoExiste_deveLancar() {
        when(tarefaRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tarefaService.atualizar(2L, TarefaRequest.builder()
                        .nome("x")
                        .status(StatusTarefa.PENDENTE)
                        .build()))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("2");
    }

    @Test
    void excluir_quandoNaoExiste_deveLancar() {
        when(tarefaRepository.existsById(5L)).thenReturn(false);

        assertThatThrownBy(() -> tarefaService.excluir(5L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void listarTodas_deveMapearLista() {
        when(tarefaRepository.findAll()).thenReturn(List.of(tarefaPersistida));

        List<TarefaResponse> lista = tarefaService.listarTodas();

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getNome()).isEqualTo("Estudar Spring");
    }
}
