package com.projeto;

import java.awt.image.BufferedImage;
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
import com.projeto.view.TelaPrincipal;


public class App
{
    public static void main( String[] args )
    {
       new TelaPrincipal();
    }

    public static long leituraQrCode(){
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
