package com.projeto.config; // Obrigatório por causa da sua pasta

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBanco {
    private static final String URL = "jdbc:mysql://localhost:3306/sys?useTimezone=true&serverTimezone=UTC";
    private static final String USUARIO = "root"; 
    private static final String SENHA = "AAssDD123";

    public static Connection conectar() {
        try {
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (SQLException e) {
            System.out.println("Erro ao conectar ao banco de dados: " + e.getMessage());
            return null;
        }
    }
}

