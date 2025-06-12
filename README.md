# Calculadora de Orçamento de Obras

> Um programa para calcular o orçamento de uma obra com base em quatro categorias: Materiais, Equipamentos, Mão de Obra e Logística. O sistema permite cadastrar itens, montar orçamentos e gerar um extrato final.

## Funcionalidades Principais

* **Cadastro por Categorias:** Permite cadastrar itens em 4 categorias distintas:
    * Materiais
    * Equipamentos
    * Mão de Obra
    * Logística
* **Criação de Orçamentos:** Possibilita a montagem de um orçamento selecionando os itens e quantidades desejadas.
* **Cálculo de Custos:**
    * Exibe o orçamento detalhado por categoria.
    * Calcula o orçamento total da obra.
    * Aplica um acréscimo de 6,5% referente a impostos sobre o valor total.
* **Edição e Validação:**
    * Permite a edição de valores e quantidades de itens já cadastrados.
    * Possui tratamento para evitar nomes duplicados e valores negativos.
* **Exportação:** Gera um arquivo `.txt` com o extrato do orçamento, contendo valores individuais e o total.

## Tecnologias Utilizadas

* **Java**: Linguagem de programação principal.
* **Java Swing**: Biblioteca para a construção da interface gráfica (Visão/View).
* **Arquitetura MVC**: Padrão de arquitetura de software utilizado para organizar o projeto.

## Estrutura do Projeto

O projeto está organizado seguindo o padrão **Model-View-Controller (MVC)** para separar as responsabilidades, facilitar a manutenção e promover a reutilização de código.

```
CalculadoraOrcamento/
|
|-- src/
|   |
|   |-- model/
|   |   |-- Categoria.java
|   |   |-- Item.java
|   |   |-- Orcamento.java
|   |   |-- ItemOrcamentado.java
|   |   `-- README.md
|   |
|   |-- view/
|   |   |-- CadastroItensPanel.java
|   |   `-- OrcamentoPanel.java
|   |
|   |-- controller/
|   |   `-- OrcamentoController.java
|   |
|   `-- Main.java
|
`-- data/
    `-- (Arquivos gerados, ex: Orcamento_1.txt)
```

### Descrição dos Pacotes

* [**`/src`**](src): Contém todo o código-fonte da aplicação.
* [**`model`**](src/model):
    * **Responsabilidade**: Representa os dados e a lógica de negócio do sistema.
    * **Conteúdo**: Classes que modelam as entidades do projeto, como `Categoria`, `Item`, `Orcamento` e `ItemOrcamentado`. Elas não têm conhecimento sobre a interface gráfica.
* [**`view`**](src/view):
    * **Responsabilidade**: Camada de apresentação, ou seja, tudo o que o usuário vê.
    * **Conteúdo**: Classes do Java Swing (`JFrame`, `JPanel`, `JButton`, etc.) que constroem as telas do sistema, como o menu inicial e as telas de cadastro.
* [**`controller`**](src/controller):
    * **Responsabilidade**: Atua como intermediário entre o `Model` e a `View`.
    * **Conteúdo**: Classes que recebem as ações do usuário (ex: um clique de botão na `View`), processam esses eventos e acionam as devidas atualizações no `Model`. Em seguida, notificam a `View` para que ela se atualize e mostre os novos dados.
* [**`Main.java`**](src/Main.java):
    * **Responsabilidade**: Ponto de entrada da aplicação. É responsável por iniciar o sistema, criando as instâncias iniciais da View e do Controller.
* [**`/data`**](data):
    * **Responsabilidade**: Diretório para armazenar dados persistentes, como os arquivos de extrato (`.txt` ou `.csv`) gerados pelo sistema.

## Como Executar

1.  Clone o repositório para sua máquina local.
2.  Abra o projeto em sua IDE Java preferida (Eclipse, IntelliJ, NetBeans).
3.  Localize o arquivo `src/Main.java`.
4.  Execute o método `main` para iniciar a aplicação.

## Autores

*(Este projeto foi desenvolvido por alunos da Universidade do Estado do Amazonas como parte de avaliação da disciplina Projeto de Programa, ministrada pelo professor Dr. Walter Charles)*

* Célio Benjamim
* Davih Taumaturgo
* Pedro Augusto
* Tiago Feitosa