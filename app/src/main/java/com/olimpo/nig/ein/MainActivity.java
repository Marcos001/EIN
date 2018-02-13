package com.olimpo.nig.ein;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.olimpo.nig.ein.NET.Producer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {

    Button send;
    Button select;
    ImageView imagem;
    Spinner sninner;
    AlertDialog alerta;

    //INT
    private static int RESULT_LOAD_IMAGE = 1;
    final int PORT = Integer.getInteger("amqp.port", 5672);
    private static final int CAMERA_REQUEST = 1888;

    //STING
    private String caminho_img = "";
    private final static String QUEUE_NAME = "pdi_cnn_respota"; //"hello"
    private String IP = "192.168.0.8";


    //METHODS

    // metodo de instancia dos componentes
    private void init(){

        sninner = (Spinner) findViewById(R.id.spinner_id);

        send = (Button) findViewById(R.id.button_id);
        send.setText(R.string.app_bt2_send);
        send.setEnabled(false);

        select = (Button) findViewById(R.id.button_selecionar);
        select.setText(R.string.app_bt1_sel);

        imagem = (ImageView) findViewById(R.id.imageView_id);

    }


    private void alerta(final String mensagem){

    MainActivity.this.runOnUiThread(new Runnable() {
        @Override
        public void run() {
            AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(MainActivity.this);
            // Setting Dialog Title
            alertDialog2.setTitle(" Classificando com CNN ");

            // Setting Dialog Message
            alertDialog2.setMessage(" Imagem classificada como: "+mensagem);

            // Setting Icon to Dialog - criar incone da aplicação
            //alertDialog2.setIcon(R.drawable.delete);

            // Showing Alert Dialog
            alertDialog2.show();
        }
    });

    }


    private void janela_get_ip(){

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {


                LayoutInflater inflater = getLayoutInflater();

                //inflamos o layout alerta.xml na view
                final View view = inflater.inflate(R.layout.dialog_signin, null);

                view.findViewById(R.id.button_ok).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        //exibe um Toast informativo.
                        try{

                            EditText ed = view.findViewById(R.id.edt_ip);
                            String ip = ed.getText().toString();
                            mensagem_toask("Setado com sucesso! = "+ip);
                            IP = ip;

                        }catch (Exception erro){
                            Log.v("Error -> ", erro.toString());
                        }
                        //desfaz o alerta.
                        alerta.dismiss();
                    }
                });


                //definimos para o botão do layout um clickListener
                view.findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        //exibe um Toast informativo.
                        mensagem_toask("Cancelado");
                        //desfaz o alerta.
                        alerta.dismiss();
                    }
                });


                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(" Setando IP ");
                builder.setView(view);
                alerta = builder.create();
                alerta.show();
            }
        });

    }


    private void servidor(){

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(IP); //ip do servidor
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
                    alerta(message);
                }
            };
            channel.basicConsume(QUEUE_NAME, true, consumer);
        }catch (Exception erro){
            Log.v("ERRO", String.valueOf(erro));
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //----------------------------------------------

        init();

        //dados do spinner
        final ArrayList<String> pal = new ArrayList<>();
        pal.add("Selecionar Imagem da Galeria");
        pal.add("Selecionar Imagem da Camera");
        ArrayAdapter opcao_selecao = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, pal);
        sninner.setAdapter(opcao_selecao);


        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (sninner.getSelectedItemPosition() == 0) {
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                    send.setEnabled(true);
                }

                if (sninner.getSelectedItemPosition() == 1) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, CAMERA_REQUEST);
                    send.setEnabled(true);
                }
            }
        });


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (send.isEnabled())
                    enviar();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                //new Consumidor(atual, getApplicationContext());
                servidor();
            }
        }).start();

        //----------------------------------------------

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.row_1) {
            //alerta("Testando 123");

            janela_get_ip();
        }
        return true;
    }

    // abrir a galeria
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

            Bitmap bmp = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

            //array de bytes da imagem
            byte[] byteArray = stream.toByteArray();

            //salvar a imagem em Path
            String path = Environment.getExternalStorageDirectory() + "";
            String name_img = String.valueOf(System.currentTimeMillis()) + ".png";
            Log.v("-->", "print do diretório de pictures = [" + path + "]");
            Log.v("-->", "nome da foto = [" + name_img + "]");
            caminho_img = "";
            caminho_img = path + "/" + name_img;
            criar_arquivo(caminho_img, byteArray);

            Log.v("--->", "Arquivo criado com sucesso em " + caminho_img + "!\n");

            // convert byte array to Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

            //Bitmap photo = (Bitmap) data.getExtras().get("data");
            imagem.setImageBitmap(bitmap);


            send.setEnabled(true);

        }

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {

            Uri selectedImage = data.getData();

            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getApplicationContext().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

            String picturePath = cursor.getString(columnIndex);
            Log.v(" Fonte da imagem => \" ", picturePath + " \" end path");
            cursor.close();

            imagem.setImageBitmap(BitmapFactory.decodeFile(picturePath));

            Log.v("-----> ", " Enviar imagem ao sevidor");
            caminho_img = "";
            caminho_img = picturePath;

            send.setEnabled(true);

        }

    }

    // criar a imagem
    public void criar_arquivo(String path_name_file, byte[] conteudo_binario) {

        try {

            FileOutputStream criar = new FileOutputStream(path_name_file);
            FileDescriptor _descricao_arquivo = criar.getFD();

            criar.write(conteudo_binario);

            criar.flush();
            _descricao_arquivo.sync();
            criar.close();

            Log.v("--->", "Imagem escrita com sucesso");


        } catch (Exception erro) {
            Log.v("--->", "erro ao criar o arquivo : " + erro);
        }
    }

    // ler arquivo
    public File ler_arquivo(String caminho) {
        File _arquivo = new File(caminho);
        return _arquivo;
    }

    // conveter arquivo para array binario
    public byte[] converte_bytes(File arquivo) {

        int tamanho = (int) arquivo.length();
        byte[] sendBuf = new byte[tamanho];
        FileInputStream inFile = null;

        try {
            inFile = new FileInputStream(arquivo);
            inFile.read(sendBuf, 0, tamanho);
            Log.v("--> ", "Arquivo convertido com sucesso");
        } catch (Exception erro) {
            Log.v("--> ", "Erro ao conveter o arquvo em bytes : " + erro);
        }
        return sendBuf;
    }

    // enviar imagem ao servidor
    private void enviar() {

        final byte[] imagem_bytes = converte_bytes(ler_arquivo(caminho_img));

        // Add permission for othres threads - abrir para criar uma thread de instancia de rede

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        new Producer(imagem_bytes);
            Log.v("---> ","imagem enviada com sucesso!");

    }

    // demostrar mensagem com toask
    private void mensagem_toask(String mensagem){
        Toast.makeText(getApplicationContext(), mensagem, Toast.LENGTH_LONG).show();
    }

    // velho quebra galho, foda-se LOG....
    private void print(String m){
        Log.v("------->", m);
    }
}
