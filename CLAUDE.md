# ProjetoL1 - Restaurante Java Swing

## Projeto
Aplicação de restaurante com cardápio e pedidos.

## Stack
- **Backend**: Java puro (pronto)
- **Frontend**: Java Swing
  - `javax.swing.*`
  - `java.awt.*`
  - `java.awt.event.*`

## Estilo
- Direto, sem explicações longas
- Código primeiro
- Sem comentários desnecessários

## Backend - Análise

### Classes

| Classe | Responsabilidade | Status |
|--------|-----------------|--------|
| `ConexaoBanco` | Conexão MySQL (hardcoded, ⚠️ inseguro) | ✅ |
| `Item` | POJO cardápio (id, nome, valor, descricao, categoria) | ✅ |
| `Pedido` | POJO pedido com Map<Item,Integer> + enum Status | ✅ |
| `Comanda` | POJO mesa (⚠️ incompleto, sem getters/setters) | ❌ |
| `PedidoDAO` | CRUD pedidos + busca por QR code | ✅ |
| `App` | Leitura QR code com webcam + ZXing | ⚠️ |

### Métodos Públicos Essenciais (Frontend)

```java
ConexaoBanco.conectar()                          // Connection | null

PedidoDAO.inserir(Pedido, Connection)            // void - com transação
PedidoDAO.busca(Connection, String qrCodigo)     // List<Pedido>
PedidoDAO.buscaCodigoQR(String, Connection)      // int idComanda (0 = não encontrado)
PedidoDAO.deletar(Connection, int id)            // void - ⚠️ BUG: coluna errada

App.leituraQrCode()                              // long - abre webcam, retorna código
```

### Problemas Críticos

- ❌ Credenciais MySQL hardcoded em `ConexaoBanco`
- ❌ `Comanda` vazio (sem getters/setters)
- ❌ `PedidoDAO.deletar()` usa coluna errada (`id` → `idPedidos`)
- ⚠️ Sem `ItemDAO` (cardápio não pode ser carregado)
- ⚠️ Sem `ComandaDAO` (mesas não podem ser listadas)
- ⚠️ `buscaCodigoQR()` retorna 0 se não encontra (ambíguo)

### Melhorias Pendentes

1. Completar `Comanda` com getters/setters
2. Criar `ItemDAO` para listar cardápio
3. Criar `ComandaDAO` para listar mesas
4. Mover credenciais para arquivo externo (`.properties`)
5. Corrigir coluna em `PedidoDAO.deletar()`
6. Separar QR code logic de `App` para classe dedicada
