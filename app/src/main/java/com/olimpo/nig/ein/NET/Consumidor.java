package com.olimpo.nig.ein.NET;

/**
 * Created by nig on 01/02/18.
 */




import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.olimpo.nig.ein.MainActivity;
import com.rabbitmq.client.*;
import java.io.IOException;


public class Consumidor {

    final int PORT = Integer.getInteger("amqp.port", 5672);
    private final static String QUEUE_NAME = "pdi_cnn_respota"; //"hello"

    private void alerta(final Activity act, final Context main, final String mensagem){

        new Thread(new Runnable() {
            @Override
            public void run() {

                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(main);
                        // Setting Dialog Title
                        alertDialog2.setTitle("Classificação CNN");

                        // Setting Dialog Message
                        alertDialog2.setMessage("Classificado em: "+mensagem);

                        // Setting Icon to Dialog
                        //alertDialog2.setIcon(R.drawable.delete);

                        // Showing Alert Dialog
                        alertDialog2.show();
                    }
                });

            }
        }).start();
    }

    public Consumidor(final Activity act, final Context main){

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("192.168.0.8"); //ip do servidor
            factory.setPort(PORT);
            factory.setUsername("nig");
            factory.setPassword("nig");
            factory.setVirtualHost("/");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();;

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    String message = new String(body, "UTF-8");
                    Log.v("------>", message);
                    alerta(act, main, message);
                }
            };
            channel.basicConsume(QUEUE_NAME, true, consumer);
        }catch (Exception erro){
            Log.v("ERRO", String.valueOf(erro));
        }
    }





}
