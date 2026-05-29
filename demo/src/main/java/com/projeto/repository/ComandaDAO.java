package com.projeto.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.projeto.model.Comanda;

public class ComandaDAO {

    public List<Comanda> listar(Connection conexao) {
        String sql = "SELECT idcomandas, codigo FROM comandas";

        List<Comanda> comandas = new ArrayList<>();

        try (PreparedStatement stm = conexao.prepareStatement(sql);
             ResultSet rs = stm.executeQuery()) {

            while (rs.next()) {
                Comanda comanda = new Comanda();
                comanda.setId(rs.getInt("idcomandas"));
                comanda.setCodigo(rs.getString("codigo"));

                comandas.add(comanda);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        return comandas;
    }

    public Comanda buscarPorCodigo(String codigo, Connection conexao) {
        String sql = "SELECT idcomandas, codigo FROM comandas WHERE codigo = ?";

        try (PreparedStatement stm = conexao.prepareStatement(sql)) {
            stm.setString(1, codigo);

            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    Comanda comanda = new Comanda();
                    comanda.setId(rs.getInt("idcomandas"));
                    comanda.setCodigo(rs.getString("codigo"));
                    return comanda;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
