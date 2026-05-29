package com.projeto.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.projeto.model.Item;
import com.projeto.model.Pedido;
import com.projeto.repository.PedidoDAO;

public class TelaComanda extends JFrame implements ActionListener {

    private String codigoQR;
    private Connection conexao;
    private DefaultTableModel modeloTabela;
    private JTable tabelaPedidos;
    private JButton btnVerItens;
    private JButton btnNovoPedido;
    private JButton btnCancelarPedido;
    private JButton btnFechar;
    private JLabel lblTotalComanda;

    public TelaComanda(String codigoQR, Connection conexao) {
        this.codigoQR = codigoQR;
        this.conexao = conexao;

        setTitle("Comanda - " + codigoQR);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel painelNorte = new JPanel();
        painelNorte.add(new JLabel("Código da Comanda: " + codigoQR));
        add(painelNorte, BorderLayout.NORTH);

        modeloTabela = new DefaultTableModel(new String[]{"Pedido#", "Status", "Valor Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaPedidos = new JTable(modeloTabela);
        JScrollPane scrollPane = new JScrollPane(tabelaPedidos);

        JPanel painelCentro = new JPanel(new BorderLayout());
        painelCentro.add(scrollPane, BorderLayout.CENTER);

        lblTotalComanda = new JLabel("Total da Comanda: R$ 0.00");
        painelCentro.add(lblTotalComanda, BorderLayout.SOUTH);

        add(painelCentro, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout());

        btnVerItens = new JButton("Ver Itens");
        btnVerItens.addActionListener(this);
        painelBotoes.add(btnVerItens);

        btnNovoPedido = new JButton("Novo Pedido");
        btnNovoPedido.addActionListener(this);
        painelBotoes.add(btnNovoPedido);

        btnCancelarPedido = new JButton("Cancelar Pedido");
        btnCancelarPedido.addActionListener(this);
        painelBotoes.add(btnCancelarPedido);

        btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(this);
        painelBotoes.add(btnFechar);

        add(painelBotoes, BorderLayout.SOUTH);

        populaPedidos();

        setVisible(true);
    }

    private void populaPedidos() {
        modeloTabela.setRowCount(0);

        PedidoDAO pedidoDAO = new PedidoDAO();
        List<Pedido> pedidos = pedidoDAO.busca(conexao, codigoQR);

        double totalComanda = 0;

        for (Pedido pedido : pedidos) {
            modeloTabela.addRow(new Object[]{
                pedido.getId(),
                pedido.getStatus().name(),
                String.format("R$ %.2f", pedido.getValorTotal())
            });

            if (pedido.getStatus() != Pedido.Status.CANCELADO) {
                totalComanda += pedido.getValorTotal();
            }
        }

        lblTotalComanda.setText(String.format("Total da Comanda: R$ %.2f", totalComanda));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnVerItens) {
            verItensPedido();
        } else if (e.getSource() == btnNovoPedido) {
            novoPedido();
        } else if (e.getSource() == btnCancelarPedido) {
            cancelarPedido();
        } else if (e.getSource() == btnFechar) {
            dispose();
        }
    }

    private void verItensPedido() {
        int linhaSelected = tabelaPedidos.getSelectedRow();
        if (linhaSelected == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um pedido");
            return;
        }

        int idPedido = (int) modeloTabela.getValueAt(linhaSelected, 0);

        PedidoDAO pedidoDAO = new PedidoDAO();
        List<Pedido> pedidos = pedidoDAO.busca(conexao, codigoQR);

        Pedido pedidoSelecionado = null;
        for (Pedido p : pedidos) {
            if (p.getId() == idPedido) {
                pedidoSelecionado = p;
                break;
            }
        }

        if (pedidoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Pedido não encontrado");
            return;
        }

        JDialog dialog = new JDialog(this, "Itens do Pedido " + idPedido);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(this);

        DefaultTableModel modeloItens = new DefaultTableModel(
            new String[]{"Item", "Quantidade", "Valor"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Item item : pedidoSelecionado.getMapa().keySet()) {
            int qtd = pedidoSelecionado.getMapa().get(item);
            double subtotal = item.getValor() * qtd;
            modeloItens.addRow(new Object[]{
                item.getNome(),
                qtd,
                String.format("R$ %.2f", subtotal)
            });
        }

        JTable tabelaItens = new JTable(modeloItens);
        JScrollPane scrollPane = new JScrollPane(tabelaItens);
        dialog.add(scrollPane);

        dialog.setVisible(true);
    }

    private void novoPedido() {
        PedidoDAO pedidoDAO = new PedidoDAO();
        int idComanda = pedidoDAO.buscaCodigoQR(codigoQR, conexao);

        if (idComanda == 0) {
            JOptionPane.showMessageDialog(this, "Erro ao obter ID da comanda");
            return;
        }

        new TelaNovoPedido(idComanda, conexao, this);
    }

    private void cancelarPedido() {
        int linhaSelected = tabelaPedidos.getSelectedRow();
        if (linhaSelected == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um pedido");
            return;
        }

        int idPedido = (int) modeloTabela.getValueAt(linhaSelected, 0);

        int confirma = JOptionPane.showConfirmDialog(this, "Deseja cancelar este pedido?");
        if (confirma == JOptionPane.YES_OPTION) {
            PedidoDAO pedidoDAO = new PedidoDAO();
            pedidoDAO.cancelar(conexao, idPedido);
            populaPedidos();
            JOptionPane.showMessageDialog(this, "Pedido cancelado");
        }
    }

    public void atualizaPedidos() {
        populaPedidos();
    }

}
