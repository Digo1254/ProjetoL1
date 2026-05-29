package com.projeto.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.projeto.model.Comanda;
import com.projeto.repository.ComandaDAO;
import com.projeto.repository.PedidoDAO;

public class TelaListaComandas extends JFrame implements ActionListener {

    private Connection conexao;
    private DefaultTableModel modeloTabela;
    private JTable tabelaComandas;
    private JButton btnAbrir;
    private JButton btnFechar;
    private JButton btnLimpar;

    public TelaListaComandas(Connection conexao) {
        this.conexao = conexao;

        setTitle("Lista de Comandas");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        modeloTabela = new DefaultTableModel(new String[]{"ID", "Código QR"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaComandas = new JTable(modeloTabela);
        JScrollPane scrollPane = new JScrollPane(tabelaComandas);
        add(scrollPane, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout());

        btnAbrir = new JButton("Abrir Comanda");
        btnAbrir.addActionListener(this);
        painelBotoes.add(btnAbrir);

        btnLimpar = new JButton("Limpar Comanda");
        btnLimpar.addActionListener(this);
        painelBotoes.add(btnLimpar);

        btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(this);
        painelBotoes.add(btnFechar);

        add(painelBotoes, BorderLayout.SOUTH);

        populaComandas();

        setVisible(true);
    }

    private void populaComandas() {
        modeloTabela.setRowCount(0);

        ComandaDAO comandaDAO = new ComandaDAO();
        List<Comanda> comandas = comandaDAO.listar(conexao);

        for (Comanda comanda : comandas) {
            modeloTabela.addRow(new Object[]{
                comanda.getId(),
                comanda.getCodigo()
            });
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAbrir) {
            abrirComanda();
        } else if (e.getSource() == btnLimpar) {
            limparComanda();
        } else if (e.getSource() == btnFechar) {
            dispose();
        }
    }

    private void abrirComanda() {
        int linhaSelected = tabelaComandas.getSelectedRow();
        if (linhaSelected == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma comanda");
            return;
        }

        String codigo = (String) modeloTabela.getValueAt(linhaSelected, 1);
        new TelaComanda(codigo, conexao);
    }

    private void limparComanda() {
        int linhaSelected = tabelaComandas.getSelectedRow();
        if (linhaSelected == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma comanda");
            return;
        }

        int idComanda = (Integer) modeloTabela.getValueAt(linhaSelected, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja limpar esta comanda?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        PedidoDAO pedidoDAO = new PedidoDAO();
        pedidoDAO.deletarPorComanda(conexao, idComanda);

        JOptionPane.showMessageDialog(this, "Comanda limpa com sucesso!");
        populaComandas();
    }

}
