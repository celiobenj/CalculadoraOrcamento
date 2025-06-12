# Pacote Controller

## Visão Geral

O pacote `controller` é responsável por intermediar a comunicação entre a interface gráfica (`view`) e a lógica de negócio (`model`) da aplicação **Calculadora de Orçamento de Obras**. Ele recebe as ações do usuário, valida e processa os dados, atualiza o modelo e solicita à interface que exiba as informações corretas.

O Controller centraliza toda a lógica de fluxo da aplicação, garantindo que a View permaneça simples e desacoplada das regras de negócio.

---

## Estrutura das Classes

O pacote possui uma classe principal de controle:

### 1. `OrcamentoController.java`

- **Responsabilidade**:  
  Gerenciar todas as operações relacionadas ao orçamento, itens e categorias, atuando como ponte entre a interface gráfica e o modelo de dados.
- **Principais Funções**:
    - Receber eventos da View (ex: cadastro, edição, remoção de itens; criação de orçamento; exportação de dados).
    - Validar dados recebidos do usuário (ex: evitar nomes duplicados, valores negativos, quantidades inválidas).
    - Atualizar as entidades do Model conforme as ações do usuário.
    - Calcular totais, aplicar impostos e preparar dados para exibição.
    - Notificar a View sobre alterações para atualização da interface.
    - Gerenciar a exportação do orçamento para arquivo `.txt` no diretório `/data`.
- **Destaques do Código**:
    - Mantém referências às listas de categorias, itens e orçamentos.
    - Implementa métodos para adicionar, editar e remover itens e categorias.
    - Realiza o cálculo do valor total do orçamento e do valor final com impostos.
    - Garante a integridade dos dados durante todo o fluxo da aplicação.

---

## Relação com Outros Pacotes

- **Model**:  
  O Controller manipula diretamente as entidades do Model, criando, alterando e consultando objetos como `Categoria`, `Item`, `Orcamento` e `ItemOrcamentado`.
- **View**:  
  Recebe comandos da View e retorna dados prontos para exibição, além de acionar atualizações na interface quando necessário.

---

## Observações

- O Controller não deve conter código de interface gráfica (ex: criação de botões, tabelas, etc).
- Toda a lógica de negócio e validação deve ser centralizada aqui, mantendo o Model limpo e a View simples.
- O Controller facilita a manutenção e evolução do sistema, permitindo alterações na lógica sem impactar a interface ou o modelo de dados.

---