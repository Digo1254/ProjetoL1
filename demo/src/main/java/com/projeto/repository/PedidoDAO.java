package com.projeto.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.projeto.model.Item;
import com.projeto.model.Pedido;

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

}
