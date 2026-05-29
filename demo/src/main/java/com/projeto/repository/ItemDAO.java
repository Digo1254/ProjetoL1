package com.projeto.repository;

import java.awt.Image;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;

import com.projeto.model.Item;

public class ItemDAO {

    public List<Item> listar(Connection conexao) {
        String sql = "SELECT idItens, nome, descricao, categoria, valor FROM itens ORDER BY categoria, nome";

        List<Item> itens = new ArrayList<>();

        try (PreparedStatement stm = conexao.prepareStatement(sql);
             ResultSet rs = stm.executeQuery()) {

            while (rs.next()) {
                Item item = new Item();
                item.setId(rs.getInt("idItens"));
                item.setNome(rs.getString("nome"));
                item.setDescricao(rs.getString("descricao"));
                item.setCategoria(rs.getString("categoria"));
                item.setValor(rs.getDouble("valor"));

                itens.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        return itens;
    }

    public ImageIcon buscarImagem(String nomeItem) {
        String nomeArquivo = nomeItem.toLowerCase().replaceAll("\\s+", "") + ".jpg";
        java.net.URL url = getClass().getClassLoader().getResource(nomeArquivo);
        if (url == null) return null;
        ImageIcon icon = new ImageIcon(url);
        Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

}
