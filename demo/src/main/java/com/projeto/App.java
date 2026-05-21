package com.projeto;

import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.projeto.config.ConexaoBanco;
import com.projeto.model.Item;
import com.projeto.model.Pedido;
import com.projeto.model.Pedido.Status;
import com.projeto.repository.PedidoDAO;


public class App 
{
    public static void main( String[] args )
    {

        String codigo = String.valueOf(leituraQrCode());

        Connection conexao = ConexaoBanco.conectar();

        PedidoDAO pedidoDAO = new PedidoDAO();

        List<Pedido> pedidosBusca = pedidoDAO.busca(conexao, codigo);

        if(pedidosBusca.isEmpty()){
            System.out.println("Está vazio");
        }
        else{
            for(Pedido pedido : pedidosBusca){
            Set<Item> set = pedido.getMapa().keySet();
            for(Item item : set){
                System.out.println("Produto: " + item.getNome() + " Quantidade: " + pedido.getMapa().get(item));
            }
            }
        }
        

        // Pedido pedido = new Pedido();

        // pedido.setIdComanda(1);
        // pedido.setStatus(Status.PREPARANDO);
        // pedido.setValorTotal(192.4);

        // Map<Item,Integer> mapa = pedido.getMapa();

        // Item item = new Item();

        // item.setId(1);

        // pedido.setId(4);

        // mapa.put(item,2);

        // pedido.setMapa(mapa);

        // pedidoDAO.inserir(pedido, conexao);
        
        // System.out.println("Tentando conectar ao MySQL...");
        
        // Connection conexao = ConexaoBanco.conectar();
        
        // if (conexao != null) {
        //     System.out.println("Conexão realizada com sucesso! 🎉");
        //     try {
        //         conexao.close();
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }
        // } else {
        //     System.out.println("Falha na conexão.");
        // }
        
        // Long resultadoTexto = leituraQrCode();
        
        // System.out.println("Codigo");
        // System.out.println(resultadoTexto);
        
    }

    private static long leituraQrCode(){
        Webcam webcam = Webcam.getDefault();
        if (webcam == null) {
            System.out.println("Nenhuma webcam.");
            return 0l;
        }

        webcam.setViewSize(WebcamResolution.VGA.getSize()); 

        WebcamPanel painel = new WebcamPanel(webcam);
        //painel.setFPSDisplayed(true);
        painel.setMirrored(true);

        JFrame janela = new JFrame("Escanear QR code");
        janela.add(painel);
        janela.pack();
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setLocationRelativeTo(null);
        janela.setVisible(true);

        SynchronousQueue<String> filaResultado = new SynchronousQueue<>();

        Thread threadLeitura = new Thread(() -> {
            try {
                while (janela.isVisible()) {
                    BufferedImage imagem = webcam.getImage();
                    if (imagem != null) {
                        try {
                            LuminanceSource source = new BufferedImageLuminanceSource(imagem);
                            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                            Result resultado = new MultiFormatReader().decode(bitmap);

                            
                            filaResultado.put(resultado.getText());
                            break;

                        } catch (NotFoundException e) {
                            
                        }
                    }
                    Thread.sleep(80);
                }
                
                
                if (!janela.isVisible()) {
                    filaResultado.put("0");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                janela.dispose();
                webcam.close();
            }
        });

        threadLeitura.setDaemon(true);
        threadLeitura.start();

        try{
            return Long.valueOf(filaResultado.take());
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
            return 0;
        }

    }
}
