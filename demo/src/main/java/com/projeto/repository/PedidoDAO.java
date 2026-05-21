package com.projeto.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.projeto.model.Item;
import com.projeto.model.Pedido;
import com.projeto.model.Pedido.Status;

public class PedidoDAO {

    public void inserir(Pedido pedido,Connection conexao){

        String sqlInsertPedidos = "INSERT INTO pedidos (valorTotal,status,fk_comanda) VALUES (?,?,?)";

        String sqlInsertItens = "INSERT INTO itens_do_pedido (pedido_id,item_id,quantidade) VALUES (?,?,?)";

        try(PreparedStatement stm = conexao.prepareStatement(sqlInsertPedidos);){

            stm.setDouble(1,pedido.getValorTotal());
            stm.setString(2,pedido.getStatus().name());
            stm.setInt(3, pedido.getIdComanda());
            stm.execute();
        }catch(Exception e){
            e.printStackTrace();

            try{
                conexao.rollback();
            }catch(SQLException e1){
                e1.printStackTrace();
            }
        }

        try(PreparedStatement stm2 = conexao.prepareStatement(sqlInsertItens);){

            for(Item item : pedido.getMapa().keySet()){
                
                stm2.setInt(1,pedido.getId());
                stm2.setInt(2, item.getId());
                stm2.setInt(3,pedido.getMapa().get(item));

                stm2.execute();

            }
        }catch(Exception e){
            e.printStackTrace();

            try{
                conexao.rollback();
            }catch(SQLException e1){
                e1.printStackTrace();
            }
        }


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

}
