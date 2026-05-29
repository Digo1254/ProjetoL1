package com.projeto.repository;

import java.awt.Image;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.projeto.config.ConexaoBanco;
import com.projeto.model.Item;
import com.projeto.model.Pedido;
import com.projeto.model.Pedido.Status;

public class PedidoDAO {

    public void inserir(Pedido pedido, Connection conexao) {

    String sqlInsertPedidos = "INSERT INTO pedidos (valorTotal, status, fk_comanda) VALUES (?, ?, ?)";
    String sqlInsertItens = "INSERT INTO itens_do_pedido (pedido_id, item_id, quantidade) VALUES (?, ?, ?)";

    try (PreparedStatement stm = conexao.prepareStatement(sqlInsertPedidos, Statement.RETURN_GENERATED_KEYS);
         PreparedStatement stm2 = conexao.prepareStatement(sqlInsertItens)) {

        
        conexao.setAutoCommit(false);

        
        stm.setDouble(1, pedido.getValorTotal());
        stm.setString(2, pedido.getStatus().name());
        stm.setInt(3, pedido.getIdComanda());
        stm.executeUpdate(); 

        
        int idPedidoGerado = 0;
        try (ResultSet generatedKeys = stm.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                idPedidoGerado = generatedKeys.getInt(1);
                
                pedido.setId(idPedidoGerado); 
            } else {
                throw new SQLException("Falha ao obter o ID do pedido criado.");
            }
        }

        
        for (Item item : pedido.getMapa().keySet()) {
            stm2.setInt(1, idPedidoGerado); 
            stm2.setInt(2, item.getId());
            stm2.setInt(3, pedido.getMapa().get(item));
            
            
            stm2.addBatch(); 
        }
        stm2.executeBatch(); 

       
        conexao.commit();

    } catch (Exception e) {
        e.printStackTrace();
        
        
        try {
            if (conexao != null) {
                conexao.rollback();
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    } finally {
        
        try {
            conexao.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


    public int buscaCodigoQR(String codigo,Connection conexao){
        String sqlBuscaCodigo = "SELECT idcomandas FROM comandas WHERE codigo = ?";

        try(PreparedStatement stm = conexao.prepareStatement(sqlBuscaCodigo);){
            stm.setString(1, codigo);
            
            try(ResultSet rs = stm.executeQuery()){
                if(rs.next()){
                    return rs.getInt("idcomandas");
                }
                

            }catch(Exception e){
                e.printStackTrace();
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return 0;
    }

    public List<Pedido> busca(Connection conexao,String qrCodigo){
        Map<Integer,Pedido> mapa = new HashMap<>();
        String sqlBuscaPedidos = "SELECT \n" + //
                        "    c.codigo,\n" + //
                        "    p.idPedidos,\n" + //
                        "    p.valorTotal,\n" + //
                        "    p.status,\n" + //
                        "    p.fk_comanda,\n" + //
                        "    i.iditens,\n" + //
                        "    i.nome,\n" + //
                        "    i.descricao,\n" + //
                        "    i.categoria,\n" + //
                        "    i.valor,\n" + //
                        "    ip.quantidade\n" + //
                        "FROM \n" + //
                        "    comandas AS c\n" + //
                        "INNER JOIN \n" + //
                        "    pedidos AS p ON c.idcomandas = p.fk_comanda\n" + //
                        "INNER JOIN \n" + //
                        "    itens_do_pedido AS ip ON p.idPedidos = ip.pedido_id\n" + //
                        "INNER JOIN \n" + //
                        "    itens AS i ON ip.item_id = i.idItens\n" + //
                        "WHERE \n" + //
                        "    c.codigo = ?;";

        try(PreparedStatement stm = conexao.prepareStatement(sqlBuscaPedidos);){
            stm.setString(1,qrCodigo);

            try(ResultSet rs = stm.executeQuery();){

                while(rs.next()){
                Pedido pedido = new Pedido();
                pedido.setId(rs.getInt("idPedidos"));
                pedido.setValorTotal(rs.getDouble("valorTotal"));
                pedido.setStatus(Status.valueOf(rs.getString("status")));
                pedido.setIdComanda(rs.getInt("fk_comanda"));

                Item item =  new Item();
                item.setId(rs.getInt("iditens"));
                item.setNome(rs.getString("nome"));
                item.setDescricao(rs.getString("descricao"));
                item.setCategoria(rs.getString("categoria"));
                item.setValor(rs.getDouble("valor"));

                if(mapa.containsKey(pedido.getId())){
                    mapa.get(pedido.getId()).getMapa().put(item, rs.getInt("quantidade"));
                    
                }
                else{
                    pedido.getMapa().put(item, rs.getInt("quantidade"));
                    mapa.put(pedido.getId(), pedido);
                }

                }
            }catch(Exception e){
                e.printStackTrace();
            }
            
            return mapa.values().stream().collect(Collectors.toList());
        }catch(Exception e){
            e.printStackTrace();
            return Collections.emptyList();
        }

    }

    public void deletar(Connection conexao, int id) {
    String sqlDeletarPedido = "DELETE FROM pedidos WHERE idPedidos = ?";

    try (PreparedStatement stm = conexao.prepareStatement(sqlDeletarPedido)) {
        stm.setInt(1, id);
        stm.executeUpdate();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public void cancelar(Connection conexao, int id) {
    String sql = "UPDATE pedidos SET status = 'CANCELADO' WHERE idPedidos = ?";

    try (PreparedStatement stm = conexao.prepareStatement(sql)) {
        stm.setInt(1, id);
        stm.executeUpdate();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public void deletarPorComanda(Connection conexao, int idComanda) {
        try {
            conexao.setAutoCommit(false);

            String sqlItens = "DELETE FROM itens_do_pedido WHERE pedido_id IN (SELECT idPedidos FROM pedidos WHERE fk_comanda = ?)";
            try (PreparedStatement stm = conexao.prepareStatement(sqlItens)) {
                stm.setInt(1, idComanda);
                stm.executeUpdate();
            }

            String sqlPedidos = "DELETE FROM pedidos WHERE fk_comanda = ?";
            try (PreparedStatement stm = conexao.prepareStatement(sqlPedidos)) {
                stm.setInt(1, idComanda);
                stm.executeUpdate();
            }

            conexao.commit();
        } catch (Exception e) {
            e.printStackTrace();
            try { conexao.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
        } finally {
            try { conexao.setAutoCommit(true); } catch (Exception e) { e.printStackTrace(); }
        }
    }

//

//     public ImageIcon buscarImagemDoBanco(int idProduto,Connection conexao) {
//         String sql = "SELECT foto FROM produtos WHERE id = ?";
    
//         
//         try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
        
//             stmt.setInt(1, idProduto);
//             ResultSet rs = stmt.executeQuery();
        
//             if (rs.next()) {
//                 
//                 InputStream input = rs.getBinaryStream("foto");
            
//                 if (input != null) {
//                     
//                     Image imagemOriginal = ImageIO.read(input);
                
//                     
//                     Image imagemRedimensionada = imagemOriginal.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                
//                     
//                     return new ImageIcon(imagemRedimensionada);
//                 }
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//         return null;
//     }


}
