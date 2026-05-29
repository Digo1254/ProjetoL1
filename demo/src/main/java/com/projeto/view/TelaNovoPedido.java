package com.projeto.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import com.projeto.model.Item;
import com.projeto.model.Pedido;
import com.projeto.model.Pedido.Status;
import com.projeto.repository.ItemDAO;
import com.projeto.repository.PedidoDAO;

public class TelaNovoPedido extends JDialog implements ActionListener {

    private int idComanda;
    private Connection conexao;
    private TelaComanda telaPai;

    private JComboBox<String> comboCategoria;
    private JPanel painelCards;
    private JScrollPane scrollCards;
    private JTextField txtQuantidade;
    private JButton btnAdicionarItem;

    private DefaultTableModel modeloPedido;
    private JTable tabelaPedidoAtual;
    private JLabel lblTotal;

    private JButton btnConfirmar;
    private JButton btnCancelar;

    private Map<Integer, Item> mapaItens;
    private Map<Integer, Integer> mapaQuantidades;
    private Item itemSelecionado;
    private JPanel cardSelecionado;

    public TelaNovoPedido(int idComanda, Connection conexao, TelaComanda telaPai) {
        super(telaPai, "Novo Pedido", true);
        this.idComanda = idComanda;
        this.conexao = conexao;
        this.telaPai = telaPai;
        this.mapaItens = new HashMap<>();
        this.mapaQuantidades = new HashMap<>();

        setSize(900, 700);
        setLocationRelativeTo(null);

        JPanel painelSuperior = new JPanel(new BorderLayout());

        JPanel painelFiltro = new JPanel(new FlowLayout());
        painelFiltro.add(new JLabel("Categoria:"));
        comboCategoria = new JComboBox<>();
        comboCategoria.addItem("Todas");
        carregarCategorias();
        comboCategoria.addActionListener(this);
        painelFiltro.add(comboCategoria);
        painelSuperior.add(painelFiltro, BorderLayout.NORTH);

        painelCards = new JPanel(new GridLayout(0, 3, 10, 10));
        painelCards.setBackground(Color.WHITE);
        scrollCards = new JScrollPane(painelCards);
        painelSuperior.add(scrollCards, BorderLayout.CENTER);

        JPanel painelInferior = new JPanel(new BorderLayout());

        JPanel painelAdicion = new JPanel(new FlowLayout());
        painelAdicion.add(new JLabel("Quantidade:"));
        txtQuantidade = new JTextField(5);
        painelAdicion.add(txtQuantidade);
        btnAdicionarItem = new JButton("Adicionar Item");
        btnAdicionarItem.addActionListener(this);
        painelAdicion.add(btnAdicionarItem);
        painelInferior.add(painelAdicion, BorderLayout.NORTH);

        modeloPedido = new DefaultTableModel(
            new String[]{"Nome", "Qtd", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaPedidoAtual = new JTable(modeloPedido);
        JScrollPane scrollPedido = new JScrollPane(tabelaPedidoAtual);
        painelInferior.add(scrollPedido, BorderLayout.CENTER);

        JPanel painelStatusEBotoes = new JPanel(new GridLayout(2, 1));

        JPanel painelStatus = new JPanel(new FlowLayout());
        lblTotal = new JLabel("Total: R$ 0.00");
        painelStatus.add(lblTotal);
        painelStatusEBotoes.add(painelStatus);

        JPanel painelBotoes = new JPanel(new FlowLayout());
        btnConfirmar = new JButton("Confirmar Pedido");
        btnConfirmar.addActionListener(this);
        painelBotoes.add(btnConfirmar);

        btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(this);
        painelBotoes.add(btnCancelar);

        painelStatusEBotoes.add(painelBotoes);
        painelInferior.add(painelStatusEBotoes, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, painelSuperior, painelInferior);
        splitPane.setDividerLocation(300);
        add(splitPane, BorderLayout.CENTER);

        carregarCardapio();

        setVisible(true);
    }

    private void carregarCategorias() {
        ItemDAO itemDAO = new ItemDAO();
        List<Item> itens = itemDAO.listar(conexao);

        List<String> categorias = new ArrayList<>();
        for (Item item : itens) {
            if (!categorias.contains(item.getCategoria())) {
                categorias.add(item.getCategoria());
            }
        }

        for (String cat : categorias) {
            comboCategoria.addItem(cat);
        }
    }

    private void carregarCardapio() {
        painelCards.removeAll();
        itemSelecionado = null;
        cardSelecionado = null;

        ItemDAO itemDAO = new ItemDAO();
        List<Item> itens = itemDAO.listar(conexao);

        String categoriaSelecionada = (String) comboCategoria.getSelectedItem();

        for (Item item : itens) {
            if (categoriaSelecionada.equals("Todas") || item.getCategoria().equals(categoriaSelecionada)) {
                criarCard(item);
            }
        }

        painelCards.revalidate();
        painelCards.repaint();
    }

    private void criarCard(Item item) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setPreferredSize(new java.awt.Dimension(200, 280));
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        card.setBackground(Color.WHITE);

        ItemDAO itemDAO = new ItemDAO();
        javax.swing.ImageIcon imagem = itemDAO.buscarImagem(item.getNome());

        JPanel painelImagem = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelImagem.setBackground(Color.WHITE);
        if (imagem != null) {
            JLabel lblImagem = new JLabel(imagem);
            painelImagem.add(lblImagem);
        } else {
            JLabel lblSemFoto = new JLabel("Sem foto");
            lblSemFoto.setHorizontalAlignment(SwingConstants.CENTER);
            painelImagem.add(lblSemFoto);
        }
        card.add(painelImagem, BorderLayout.NORTH);

        JPanel painelInfo = new JPanel(new GridLayout(3, 1, 0, 2));
        painelInfo.setBackground(Color.WHITE);

        JLabel lblNome = new JLabel("<html><b>" + item.getNome() + "</b></html>");
        lblNome.setHorizontalAlignment(SwingConstants.CENTER);
        painelInfo.add(lblNome);

        JLabel lblDescricao = new JLabel("<html><font size='2'>" + item.getDescricao() + "</font></html>");
        lblDescricao.setHorizontalAlignment(SwingConstants.CENTER);
        painelInfo.add(lblDescricao);

        JLabel lblPreco = new JLabel(String.format("R$ %.2f", item.getValor()));
        lblPreco.setHorizontalAlignment(SwingConstants.CENTER);
        painelInfo.add(lblPreco);

        card.add(painelInfo, BorderLayout.CENTER);

        card.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selecionarCard(card, item);
            }

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}
        });

        painelCards.add(card);
    }

    private void selecionarCard(JPanel card, Item item) {
        if (cardSelecionado != null) {
            cardSelecionado.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        }

        cardSelecionado = card;
        itemSelecionado = item;
        card.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == comboCategoria) {
            carregarCardapio();
        } else if (e.getSource() == btnAdicionarItem) {
            adicionarItem();
        } else if (e.getSource() == btnConfirmar) {
            confirmarPedido();
        } else if (e.getSource() == btnCancelar) {
            dispose();
        }
    }

    private void adicionarItem() {
        if (itemSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um item");
            return;
        }

        String qtdStr = txtQuantidade.getText();
        if (qtdStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite a quantidade");
            return;
        }

        try {
            int qtd = Integer.parseInt(qtdStr);
            if (qtd <= 0) {
                JOptionPane.showMessageDialog(this, "Quantidade deve ser maior que 0");
                return;
            }

            mapaItens.put(itemSelecionado.getId(), itemSelecionado);
            mapaQuantidades.put(itemSelecionado.getId(), qtd);

            double subtotal = itemSelecionado.getValor() * qtd;
            modeloPedido.addRow(new Object[]{
                itemSelecionado.getNome(),
                qtd,
                String.format("R$ %.2f", subtotal)
            });

            atualizaTotal();
            txtQuantidade.setText("");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantidade deve ser um número");
        }
    }

    private void atualizaTotal() {
        double total = 0;
        for (Integer itemId : mapaItens.keySet()) {
            Item item = mapaItens.get(itemId);
            int qtd = mapaQuantidades.get(itemId);
            total += item.getValor() * qtd;
        }
        lblTotal.setText(String.format("Total: R$ %.2f", total));
    }

    private void confirmarPedido() {
        if (mapaItens.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Adicione ao menos um item");
            return;
        }

        Pedido pedido = new Pedido();
        pedido.setIdComanda(idComanda);
        pedido.setStatus(Status.PREPARANDO);

        double total = 0;
        Map<Item, Integer> mapaPedido = new HashMap<>();
        for (Integer itemId : mapaItens.keySet()) {
            Item item = mapaItens.get(itemId);
            int qtd = mapaQuantidades.get(itemId);
            mapaPedido.put(item, qtd);
            total += item.getValor() * qtd;
        }

        pedido.setMapa(mapaPedido);
        pedido.setValorTotal(total);

        PedidoDAO pedidoDAO = new PedidoDAO();
        pedidoDAO.inserir(pedido, conexao);

        JOptionPane.showMessageDialog(this, "Pedido confirmado!");
        telaPai.atualizaPedidos();
        dispose();
    }

}
