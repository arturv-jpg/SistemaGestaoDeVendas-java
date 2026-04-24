# 🛒 Sistema de Gestão de Vendas e Estoque

Este projeto é uma solução desenvolvida em **JavaFX** para gerenciamento de vendas, produtos e controle de estoque, permitindo a automação de processos comerciais e operacionais.

---

## 🚀 Apresentação

O sistema está totalmente funcional e demonstra o fluxo completo de uma operação:

* Login com controle de acesso por tipo de usuário
* Cadastro e gerenciamento de produtos
* Controle de estoque com entradas e saídas
* Registro de movimentações com identificação do usuário
* Atualização automática do estoque

A aplicação simula um ambiente real de gestão comercial.

---

## ⚙️ Funcionalidades Implementadas

### 📦 Gestão de Estoque & Produtos

* **Cadastro de Produtos**

  * Nome, código de barras, categoria, descrição
  * Preço de venda, preço de custo e margem de lucro
  * Definição de estoque mínimo

* **Cálculo Automático de Preço**

  * Preço de venda calculado com base em custo + margem

* **Alerta de Estoque Baixo**

  * Destaque visual em vermelho quando a quantidade ≤ estoque mínimo

* **Exclusão Inteligente**

  * Produtos sem vínculo são excluídos
  * Produtos com histórico podem ser desativados (boa prática)

---

### 📊 Controle de Estoque

* **Entrada e Saída de Produtos**

  * Atualização automática da quantidade

* **Validação de Estoque**

  * Impede saída maior que a quantidade disponível

* **Histórico de Movimentações**

  * Registro com:

    * Produto
    * Quantidade
    * Tipo (Entrada/Saída)
    * Data e hora
    * Usuário responsável

---

### 🔐 Segurança e Acessos

* **Níveis de Permissão**

  * Admin
  * Gerente
  * Vendedor
  * Estoquista

* **Interface Dinâmica**

  * Botões e menus são exibidos conforme permissões do usuário

---

## 🔄 Fluxo do Sistema

1. **Login**

   * Usuário acessa com credenciais
   * Sistema aplica permissões automaticamente

2. **Cadastro de Produtos**

   * Inserção e edição de produtos
   * Definição de preços e estoque

3. **Processamento de Estoque**

   * Entrada de mercadorias
   * Saída com validação de quantidade

4. **Monitoramento**

   * Visualização em tabela
   * Destaque de produtos com estoque baixo

---

## 🛠️ Tecnologias Utilizadas

* Java
* JavaFX
* MySQL
* JDBC

---

## 🧩 Pré-requisitos e Instalação

### 📌 Banco de Dados (MySQL)

1. Crie um banco chamado:

```
sistema
```

2. Execute o script SQL do projeto (tabelas: produto, usuario, venda, etc.)

3. Configure as credenciais na classe de conexão:

```java
private static final String USER = "seu_usuario";
private static final String PASS = "sua_senha";
```

---

### ⚙️ JavaFX

Certifique-se de ter o JavaFX configurado na sua IDE.

Se necessário, use VM Arguments:

```
--module-path "caminho/javafx/lib" --add-modules javafx.controls,javafx.fxml
```

---

### 📦 Bibliotecas

* mysql-connector-j
* JavaFX (controls, fxml, graphics, base)

---

## ▶️ Como Executar

1. Importar o projeto na IDE (Eclipse ou IntelliJ)
2. Iniciar o MySQL
3. Executar a classe principal (`Main`)

---

## 🎓 Instruções para Avaliação

* Utilize o usuário padrão cadastrado no banco (ex: admin)
* Teste o fluxo:

  * Cadastro de produto
  * Entrada no estoque
  * Saída de produtos
* Tente inserir quantidade maior que o estoque para validar bloqueio
* Observe o alerta visual de estoque mínimo

-- CODIGO DO BANCO DE DADOS(MYSQL)
create database sistema;
use sistema;

CREATE TABLE IF NOT EXISTS usuario (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    login VARCHAR(50) NOT NULL UNIQUE,
    senha VARCHAR(100) NOT NULL,
    tipo ENUM('admin', 'gerente', 'vendedor', 'estoquista') NOT NULL
);

CREATE TABLE IF NOT EXISTS cliente (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,      
    email VARCHAR(100) NOT NULL UNIQUE,
    status ENUM('Ativo', 'Inativo') DEFAULT 'Ativo'
);

CREATE TABLE IF NOT EXISTS produto (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    codigo_barras VARCHAR(50) NOT NULL UNIQUE,
    descricao TEXT,
    categoria VARCHAR(50),
    preco DECIMAL(10,2) NOT NULL,
    quantidade INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS venda (
    id INT PRIMARY KEY AUTO_INCREMENT,
    idCliente INT NOT NULL,
    data DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valor DECIMAL(10,2) NOT NULL,
    status ENUM('FINALIZADA', 'CANCELADA') DEFAULT 'FINALIZADA',
    motivo_cancelamento TEXT,
    FOREIGN KEY (idCliente) REFERENCES cliente(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS item_venda (
    idVenda INT NOT NULL,
    idProduto INT NOT NULL,
    quantidade INT NOT NULL,
    preco DECIMAL(10,2) NOT NULL,   -- preço unitário no momento da venda
    PRIMARY KEY (idVenda, idProduto),
    FOREIGN KEY (idVenda) REFERENCES venda(id) ON DELETE CASCADE,
    FOREIGN KEY (idProduto) REFERENCES produto(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS movimentacaoEstoque (
    id INT PRIMARY KEY AUTO_INCREMENT,
    idProduto INT NOT NULL,
    dataHora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    quantidade INT NOT NULL,
    tipo TINYINT NOT NULL CHECK (tipo IN (0,1)),
    FOREIGN KEY (idProduto) REFERENCES produto(id) ON DELETE CASCADE
);

CREATE INDEX idx_cliente_nome ON cliente(nome);
CREATE INDEX idx_cliente_cpf ON cliente(cpf);
CREATE INDEX idx_produto_nome ON produto(nome);
CREATE INDEX idx_produto_codigo_barras ON produto(codigo_barras);
CREATE INDEX idx_venda_idCliente ON venda(idCliente);
CREATE INDEX idx_venda_data ON venda(data);
CREATE INDEX idx_movimentacao_idProduto ON movimentacaoEstoque(idProduto);
CREATE INDEX idx_movimentacao_dataHora ON movimentacaoEstoque(dataHora);

-- ================= INSERTS INICIAIS (OPCIONAIS) =================
-- Usuário padrão: admin / admin (senha sem criptografia – para testes)
INSERT INTO usuario (nome, login, senha, tipo) 
VALUES ('Administrador', 'admin', '123', 'admin')
ON DUPLICATE KEY UPDATE id=id;

-- Cliente de exemplo
INSERT INTO cliente (nome, cpf, email, status)
VALUES ('Cliente Padrão', '111.111.111-11', 'cliente@exemplo.com', 'Ativo')
ON DUPLICATE KEY UPDATE id=id;

-- Produto de exemplo
INSERT INTO produto (nome, codigo_barras, descricao, categoria, preco, quantidade)
VALUES ('Produto Exemplo', '7891234567890', 'Descrição do produto exemplo', 'Categoria A', 19.90, 100)
ON DUPLICATE KEY UPDATE id=id;

SELECT * FROM usuario;

-- Adicionar colunas para precificação e estoque mínimo
ALTER TABLE produto ADD COLUMN preco_custo DECIMAL(10,2) NOT NULL DEFAULT 0;
ALTER TABLE produto ADD COLUMN margem_lucro DECIMAL(5,2) NOT NULL DEFAULT 0;
ALTER TABLE produto ADD COLUMN estoque_minimo INT NOT NULL DEFAULT 0;

UPDATE cliente SET cpf = REPLACE(REPLACE(REPLACE(REPLACE(cpf, '.', ''), '-', ''), '/', ''), ' ', '') WHERE id > 0;

-- Adicionar colunas na tabela venda
ALTER TABLE venda ADD COLUMN desconto DECIMAL(10,2) NOT NULL DEFAULT 0;
ALTER TABLE venda ADD COLUMN valor_pago DECIMAL(10,2) NOT NULL DEFAULT 0;
ALTER TABLE venda ADD COLUMN troco DECIMAL(10,2) NOT NULL DEFAULT 0;

-- Tabela de formas de pagamento por venda
CREATE TABLE IF NOT EXISTS pagamento_venda (
    id INT PRIMARY KEY AUTO_INCREMENT,
    idVenda INT NOT NULL,
    forma ENUM('dinheiro', 'cartao_debito', 'cartao_credito') NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (idVenda) REFERENCES venda(id) ON DELETE CASCADE
);

SELECT id, nome, login, tipo FROM usuario;

DELETE FROM usuario WHERE id = 4;

-- Adicionar coluna idUsuario na tabela movimentacaoEstoque
ALTER TABLE movimentacaoEstoque ADD COLUMN idUsuario INT;
ALTER TABLE movimentacaoEstoque ADD FOREIGN KEY (idUsuario) REFERENCES usuario(id);

SELECT m.id, m.idProduto, m.quantidade, m.tipo, m.dataHora, m.idUsuario, u.nome as usuario_nome
FROM movimentacaoEstoque m
LEFT JOIN usuario u ON m.idUsuario = u.id
ORDER BY m.id DESC
LIMIT 5;

SELECT id, nome, quantidade FROM produto;

ALTER TABLE produto ADD COLUMN ativo BOOLEAN DEFAULT TRUE;

SELECT * FROM produto WHERE ativo = true;
