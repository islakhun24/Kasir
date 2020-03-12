package com.example.kasir;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.voela.iduangs.IDUang;

public class Kembalian extends AppCompatActivity {

    private static final String URL_CART = "https://geprekrame.doktersoftware.my.id/getDetailPesanan.php";
    private static final String URL_UPDATE = "https://geprekrame.doktersoftware.my.id/update.php";
    TextView kembalian,nama,atasNama, namakasir;
    Button oke,reprint;

    CardView cvList;
    List<Pesanan> pesananList2;
    private RecyclerView recyclerView;
    private LinearLayoutManager llm;
    private DividerItemDecoration did;
    private RecyclerView.Adapter adapter;
    String atasnama = "";

    byte FONT_TYPE;
    private static BluetoothSocket btsocket;
    private static OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kembalian);

        Intent intent = getIntent();
        final String extraNmae = intent.getStringExtra("name");
        final String kembalian2 = intent.getStringExtra("kembalian");
        final int kembalian3 = Integer.valueOf(kembalian2);

        final String tanggal = intent.getStringExtra("tanggal");
        final String totalbayar = intent.getStringExtra("totalbayar");
        final String bayarcustomer = intent.getStringExtra("bayarcustomer");
        atasnama = getIntent().getStringExtra("atasnama");


        kembalian = findViewById(R.id.tvKembalian);
        nama = findViewById(R.id.tvnamakasir);
        oke = findViewById(R.id.btnOke);
        reprint = findViewById(R.id.btnReprint);
        atasNama = findViewById(R.id.tvAtasNama);
        namakasir = findViewById(R.id.tvnamakasir);

        recyclerView = findViewById(R.id.recyclerView);
        cvList = findViewById(R.id.cvList2);
        pesananList2 = new ArrayList<>();
        adapter = new AdapterDetailPesanan(this.getApplicationContext(),pesananList2);
        llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        did = new DividerItemDecoration(recyclerView.getContext(), llm.getOrientation());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(llm);
        recyclerView.addItemDecoration(did);

        kembalian.setText(IDUang.parsingRupiah(kembalian3)+",-");
        nama.setText(extraNmae);
        atasNama.setText(atasnama);


        loadDetailPesanan();

        oke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update();

            }
        });

        reprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printDemo();
            }
        });
    }

    public void onBackPressed() {

    }

    private void loadDetailPesanan() {

        StringRequest jsonObjectRequest = new StringRequest(com.android.volley.Request.Method.GET,URL_CART+"?atasnama="+atasnama, new Response.Listener<String>(){

            @Override
            public void onResponse(String response) {
                Log.e("Aaaa",response);
                try{
                    JSONObject jsonObject = new JSONObject(response);
                    int success = jsonObject.getInt("success");
                    if (success == 1) {
                        JSONArray jsonArray = jsonObject.getJSONArray("result");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                JSONObject object = jsonArray.getJSONObject(i);
                                Pesanan pesanan = new Pesanan();
                                pesanan.setId(object.getInt("id"));
                                pesanan.setNama(object.getString("nama"));
                                pesanan.setHarga(object.getString("harga"));
                                pesanan.setQty(object.getString("qty"));
                                pesanan.setSubtotal(object.getString("subtotal"));
                                pesanan.setKeterangan(object.getString("keterangan"));
                                pesananList2.add(pesanan);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", error.toString());
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    public void update(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPDATE+"?atasnama="+atasnama
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                final String name = nama.getText().toString().trim();
                Intent intent = new Intent(Kembalian.this, Home.class);
                intent.putExtra("email",name);
                startActivity(intent);
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", error.toString());
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("atasnama", atasnama);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(Kembalian.this);
        requestQueue.add(stringRequest);
    }

    protected void printDemo() {

        Intent intent = getIntent();
        final String extraNmae = intent.getStringExtra("name");

        final String string = intent.getStringExtra("kembalian");
        final int kembalian = Integer.valueOf(string);

        final String tanggal = intent.getStringExtra("tanggal");

        final String string2 = intent.getStringExtra("totalbayar");
        final int totalbayar = Integer.valueOf(string2);

        final String string3 = intent.getStringExtra("bayarcustomer");
        final int bayarcustomer = Integer.valueOf(string3);
        atasnama = getIntent().getStringExtra("atasnama");

        if(btsocket == null){
            Intent BTIntent = new Intent(getApplicationContext(), DeviceList.class);
            this.startActivityForResult(BTIntent, DeviceList.REQUEST_CONNECT_BT);
        }else {
            OutputStream opstream = null;
            try {
                opstream = btsocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream = opstream;

            //print command
            try {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                outputStream = btsocket.getOutputStream();

                byte[] printformat = {0x1B, 0 * 21, FONT_TYPE};
                //outputStream.write(printformat);

                //print title
                printCustom("GEPREK RAME", 0, 1);
                printCustom("Jalan Raya Pesanggrahan", 0, 1);
                printCustom("Kesugihan", 0, 1);
                printCustom("__________________________________________", 0, 1);
                printCustom(tanggal + "                             " + extraNmae, 0, 1);
                printCustom("__________________________________________", 0, 1);

                for (int i = 0; i < pesananList2.size(); i++) {
                    printCustom(pesananList2.get(i).getNama()+ "( "+ pesananList2.get(i).getKeterangan() + " )", 0, 0);
                    printCustom(pesananList2.get(i).getQty() + " X " + IDUang.parsingRupiah(Integer.parseInt(pesananList2.get(i).getHarga()))
                            + "                 " + IDUang.parsingRupiah(Integer.parseInt(pesananList2.get(i).getSubtotal())), 0, 0);
                }

                printCustom("__________________________________________", 0, 1);
                printCustom("Total                         " + IDUang.parsingRupiah(totalbayar), 0, 0);
                printCustom("Bayar(Cash)                   " + IDUang.parsingRupiah(bayarcustomer), 0, 0);
                printCustom("Kembali                       " + IDUang.parsingRupiah(kembalian), 0, 0);
                printNewLine();
                printNewLine();
                printCustom("Terima Kasih Atas Kunjungannya", 0, 1);
                printCustom("Menerima Pesanan", 0, 1);
                printCustom("082135406709", 0, 1);
                printCustom("__________________________________________", 0, 1);
                printNewLine();
                printNewLine();


                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //print custom
    private void printCustom(String msg, int size, int align) {
        //Print config "mode"
        byte[] cc = new byte[]{0x1B,0x21,0x03};  // 0- normal size text
        //byte[] cc1 = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
        byte[] bb = new byte[]{0x1B,0x21,0x08};  // 1- only bold text
        byte[] bb2 = new byte[]{0x1B,0x21,0x20}; // 2- bold with medium text
        byte[] bb3 = new byte[]{0x1B,0x21,0x10}; // 3- bold with large text
        try {
            switch (size){
                case 0:
                    outputStream.write(cc);
                    break;
                case 1:
                    outputStream.write(bb);
                    break;
                case 2:
                    outputStream.write(bb2);
                    break;
                case 3:
                    outputStream.write(bb3);
                    break;
            }

            switch (align){
                case 0:
                    //left align
                    outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                    break;
                case 1:
                    //center align
                    outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                    break;
                case 2:
                    //right align
                    outputStream.write(PrinterCommands.ESC_ALIGN_RIGHT);
                    break;
            }
            outputStream.write(msg.getBytes());
            outputStream.write(PrinterCommands.LF);
            //outputStream.write(cc);
            //printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //print photo
    public void printPhoto(int img) {
        try {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                    img);
            if(bmp!=null){
                byte[] command = Utils.decodeBitmap(bmp);
                outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                printText(command);
            }else{
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }
    }

    //print unicode
    public void printUnicode(){
        try {
            outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
            printText(Utils.UNICODE_TEXT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //print new line
    private void printNewLine() {
        try {
            outputStream.write(PrinterCommands.FEED_LINE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void resetPrint() {
        try{
            outputStream.write(PrinterCommands.ESC_FONT_COLOR_DEFAULT);
            outputStream.write(PrinterCommands.FS_FONT_ALIGN);
            outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
            outputStream.write(PrinterCommands.ESC_CANCEL_BOLD);
            outputStream.write(PrinterCommands.LF);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //print text
    private void printText(String msg) {
        try {
            // Print normal text
            outputStream.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //print byte[]
    private void printText(byte[] msg) {
        try {
            // Print normal text
            outputStream.write(msg);
            printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String leftRightAlign(String str1, String str2) {
        String ans = str1 +str2;
        if(ans.length() <31){
            int n = (31 - str1.length() + str2.length());
            ans = str1 + new String(new char[n]).replace("\0", " ") + str2;
        }
        return ans;
    }


    private String[] getDateTime() {
        final Calendar c = Calendar.getInstance();
        String dateTime [] = new String[2];
        dateTime[0] = c.get(Calendar.DAY_OF_MONTH) +"/"+ c.get(Calendar.MONTH) +"/"+ c.get(Calendar.YEAR);
        dateTime[1] = c.get(Calendar.HOUR_OF_DAY) +":"+ c.get(Calendar.MINUTE);
        return dateTime;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(btsocket!= null){
                outputStream.close();
                btsocket.close();
                btsocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            btsocket = DeviceList.getSocket();
            if(btsocket != null){

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
