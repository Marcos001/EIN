package com.olimpo.nig.ein.NET;


import android.util.Log;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Created by mrv on 24/04/17.
 */

/**
 * sudo ufw allow 5672
 * habilitar essa porta para receber
 * */

public class Producer {

    private final static String QUEUE_NAME = "pdi"; //"hello"
    private static final int PORT = Integer.getInteger("amqp.port", 5672);
    static final String EXCHANGE = System.getProperty("amqp.exchange", "systemExchange");
    static final String ENCODING = "UTF-8";


    public Producer(byte[] fileData){

        String HOST = System.getProperty("amqp.host", "192.168.0.8");

        try{

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(HOST);
            factory.setPort(PORT);
            factory.setUsername("nig");
            factory.setPassword("nig");
            factory.setVirtualHost("/");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            print(" enviano mensagem > ");
            String saudacao = "Comunicação feita com sucesso! [ ANDROID - KUBUNTU ]";
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, fileData);
            //channel.basicPublish("", QUEUE_NAME, null, saudacao.getBytes());//"UTF-8"));
            System.out.println(" [x] Arquivo Enviado de Publish()  ");

            print(" fechando conexão > ");
            channel.close();
            connection.close();


        }catch (Exception erro){
            System.out.println("Erro ao instanciar publisher enviando ao cliente >"+ HOST +" \n"+erro);
        }

    }


    private void print(String m){
        Log.v("====>", m);
    }

}