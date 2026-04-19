-- Execute este script já conectado ao banco lista_tarefas

CREATE TABLE IF NOT EXISTS tarefa (
    id               BIGSERIAL       PRIMARY KEY,
    nome             VARCHAR(200)    NOT NULL,
    descricao        TEXT,
    status           VARCHAR(30)     NOT NULL,
    observacoes      TEXT,
    data_criacao     TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    data_atualizacao TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tarefa_status ON tarefa (status);

COMMENT ON TABLE tarefa IS 'Tarefas do dia a dia (sem vínculo com usuário)';
COMMENT ON COLUMN tarefa.nome IS 'Nome curto da tarefa';
COMMENT ON COLUMN tarefa.descricao IS 'Descrição detalhada';
COMMENT ON COLUMN tarefa.status IS 'Valores: PENDENTE, EM_ANDAMENTO, CONCLUIDA, CANCELADA';
COMMENT ON COLUMN tarefa.observacoes IS 'Notas adicionais';
COMMENT ON COLUMN tarefa.data_criacao IS 'Auditoria JPA na aplicação complementa na inserção';
COMMENT ON COLUMN tarefa.data_atualizacao IS 'Auditoria JPA na aplicação em cada atualização';
