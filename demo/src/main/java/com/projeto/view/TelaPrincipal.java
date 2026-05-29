package com.projeto.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.projeto.App;
import com.projeto.config.ConexaoBanco;
import com.projeto.repository.ComandaDAO;
import com.projeto.repository.PedidoDAO;

public class TelaPrincipal extends JFrame implements ActionListener {

    private Connection conexao;
    private JMenuItem escanearQR;
    private JMenuItem listarComandas;
    private JMenuItem novoPedido;
    private JMenuItem sair;

    public TelaPrincipal() {
        conexao = ConexaoBanco.conectar();

        setTitle("Restaurante - Sistema de Pedidos");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu menuComandas = new JMenu("Comandas");
        menuBar.add(menuComandas);

        escanearQR = new JMenuItem("Escanear QR Code");
        escanearQR.addActionListener(this);
        menuComandas.add(escanearQR);

        listarComandas = new JMenuItem("Listar Comandas");
        listarComandas.addActionListener(this);
        menuComandas.add(listarComandas);

        JMenu menuPedidos = new JMenu("Pedidos");
        menuBar.add(menuPedidos);

        novoPedido = new JMenuItem("Novo Pedido");
        novoPedido.addActionListener(this);
        menuPedidos.add(novoPedido);

        JMenu menuArquivo = new JMenu("Arquivo");
        menuBar.add(menuArquivo);

        sair = new JMenuItem("Sair");
        sair.addActionListener(this);
        menuArquivo.add(sair);

        JLabel label = new JLabel("Sistema de Restaurante");
        label.setHorizontalAlignment(JLabel.CENTER);
        add(label);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == escanearQR) {
            escanearQRCode();
        } else if (e.getSource() == listarComandas) {
            abrirListaComandas();
        } else if (e.getSource() == novoPedido) {
            JOptionPane.showMessageDialog(this, "Selecione uma comanda primeiro");
        } else if (e.getSource() == sair) {
            fecharConexao();
            System.exit(0);
        }
    }

    private void escanearQRCode() {
        new SwingWorker<String, Void>() {
            protected String doInBackground() {
                long codigo = App.leituraQrCode();
                if (codigo == 0) {
                    return null;
                }

                String codigoQR = String.valueOf(codigo);
                PedidoDAO pedidoDAO = new PedidoDAO();
                int idComanda = pedidoDAO.buscaCodigoQR(codigoQR, conexao);

                if (idComanda == 0) {
                    return null;
                }

                return codigoQR;
            }

            protected void done() {
                try {
                    String codigoQR = get();
                    if (codigoQR != null) {
                        new TelaComanda(codigoQR, conexao);
                    } else {
                        JOptionPane.showMessageDialog(TelaPrincipal.this, "Comanda não encontrada");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void abrirListaComandas() {
        new TelaListaComandas(conexao);
    }

    private void fecharConexao() {
        if (conexao != null) {
            try {
                conexao.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
