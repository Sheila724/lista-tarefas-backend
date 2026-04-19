-- Execute conectado ao banco "postgres" (ou outro banco template) com usuário que possa criar databases.
-- Depois conecte-se ao banco lista_tarefas e execute schema.sql

CREATE DATABASE lista_tarefas
    WITH OWNER = postgres
    ENCODING = 'UTF8'
    TEMPLATE = template0;
