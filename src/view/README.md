# Pacote View

## Visão Geral

O pacote `view` contém todas as classes responsáveis pela interface gráfica do usuário (GUI) da aplicação **Calculadora de Orçamento de Obras**. Utiliza a biblioteca Java Swing para criar e organizar as telas, painéis, botões, tabelas e demais componentes visuais. O objetivo é proporcionar uma experiência intuitiva para o usuário, facilitando o cadastro de itens, montagem de orçamentos e visualização dos resultados.

As classes da `view` não implementam regras de negócio, apenas exibem informações e capturam as ações do usuário, repassando-as para o Controller.

---

## Estrutura das Classes

O pacote é composto por três classes principais, cada uma com uma função específica na interface do sistema.

### 1. `CadastroItensPanel.java`

- **Responsabilidade**:  
  Painel dedicado ao cadastro, edição e exclusão de itens em cada categoria (Materiais, Equipamentos, Mão de Obra, Logística).
- **Principais Componentes**:
    - Campos de texto para descrição, valor e quantidade do item.
    - ComboBox para seleção da categoria.
    - Botões para adicionar, editar e remover itens.
    - Tabela (`JTable`) para exibir os itens cadastrados, com renderização customizada para valores monetários.
- **Destaques do Código**:
    - Utiliza `DecimalFormatRenderer` para exibir valores formatados na tabela.
    - Implementa listeners para tratar eventos de clique nos botões e seleção de linhas na tabela.
    - Validações visuais para evitar cadastro de itens com nomes duplicados ou valores inválidos.

### 2. `OrcamentoPanel.java`

- **Responsabilidade**:  
  Painel para montagem do orçamento, seleção de itens, definição de quantidades e visualização do orçamento detalhado.
- **Principais Componentes**:
    - Tabela para seleção de itens e inserção de quantidades.
    - Exibição do orçamento agrupado por categoria.
    - Campo para mostrar o valor total do orçamento e o valor final com impostos.
    - Botão para exportar o orçamento para arquivo `.txt`.
- **Destaques do Código**:
    - Atualiza dinamicamente os totais conforme o usuário altera quantidades.
    - Exibe mensagens de erro caso o usuário tente inserir quantidades inválidas.
    - Chama métodos do Controller para processar e salvar o orçamento.

### 3. `TelaPrincipal.java`

- **Responsabilidade**:  
  Janela principal da aplicação, responsável por gerenciar a navegação entre os painéis de cadastro e orçamento.
- **Principais Componentes**:
    - Menu inicial com opções para acessar o cadastro de itens ou a montagem de orçamento.
    - Gerenciamento de troca de painéis usando `CardLayout` ou similar.
    - Exibição de mensagens de boas-vindas e informações do sistema.
- **Destaques do Código**:
    - Centraliza a inicialização da interface.
    - Garante que apenas um painel seja exibido por vez.
    - Encaminha as ações do usuário para o Controller.

---

## Relação com Outros Pacotes

- **Model**:  
  A `view` apenas exibe dados vindos do `model`, sem manipular diretamente as entidades.
- **Controller**:  
  Toda ação do usuário (cliques, inserções, edições) é encaminhada ao Controller, que processa e retorna os dados para atualização da interface.

---

## Observações

- O código da `view` deve ser o mais desacoplado possível do `model` e do `controller`, facilitando futuras alterações na interface sem impactar a lógica de negócio.
- Não implemente regras de negócio ou persistência de dados neste pacote.

---