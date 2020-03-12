package com.example.kasir;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import id.voela.iduangs.IDUang;

public class PesananDetailActivity extends AppCompatActivity {

    private static final String URL_CART = "https://geprekrame.doktersoftware.my.id/getDetailPesanan.php";
    private static final String URL_SUM = "https://geprekrame.doktersoftware.my.id/totalbayarkasir.php";
    private static final String URL_BAYAR = "https://geprekrame.doktersoftware.my.id/upload_laporan.php";
    TextView totalbayar, tvQty, tvName, tvHarga, tvSubTotal, date, namakasir;
    Button bayar;
    EditText etBayar;
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
        setContentView(R.layout.activity_pesanan_detail);

        recyclerView = findViewById(R.id.recyclerView);
        totalbayar = findViewById(R.id.tvTotalBayar);
        bayar = findViewById(R.id.btnBayar);
        date = findViewById(R.id.date);
        tvQty = findViewById(R.id.tvQty);
        tvName = findViewById(R.id.tvNamamenu);
        tvHarga = findViewById(R.id.textViewHarga);
        tvSubTotal = findViewById(R.id.textViewSubtotal);
        etBayar = findViewById(R.id.etBayar);
        namakasir = findViewById(R.id.tvNamaKasir);
        cvList = findViewById(R.id.cvList2);
        pesananList2 = new ArrayList<>();
        adapter = new AdapterDetailPesanan(this.getApplicationContext(),pesananList2);
        llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        did = new DividerItemDecoration(recyclerView.getContext(), llm.getOrientation());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(llm);
        recyclerView.addItemDecoration(did);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String currentDateandTime = sdf.format(new Date());
        date.setText(currentDateandTime);
        atasnama = getIntent().getStringExtra("atasnama");

        Intent intent = getIntent();
        final String extraNmae = intent.getStringExtra("name");

        namakasir.setText(extraNmae);
        etBayar.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().equals(current)) {
                    etBayar.removeTextChangedListener(this);

                    String replaceable = String.format("[%s,.\\s]", NumberFormat.getCurrencyInstance().getCurrency().getSymbol());
                    String cleanString = editable.toString().replaceAll(replaceable, "");

                    double parsed;
                    try {
                        parsed = Double.parseDouble(cleanString);
                    } catch (NumberFormatException e) {
                        parsed = 0.000;
                    }
                    NumberFormat formatter = NumberFormat.getCurrencyInstance();
                    formatter.setMaximumFractionDigits(0);
                    String formatted = formatter.format((parsed));

                    current = formatted;
                    etBayar.setText(formatted);
                    etBayar.setSelection(formatted.length());
                    etBayar.addTextChangedListener(this);
                }
            }
        });
        loadDetailPesanan();
        sum();

        bayar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String string1 = totalbayar.getText().toString().trim();
                final String string = etBayar.getText().toString().trim();
                final String bayarCondition2 = string.replaceAll("[^0-9]", "");
                final String totalBayar = string1.replaceAll("[^0-9]", "");

                int bayarCondition3 = Integer.valueOf(bayarCondition2);
                int totalBayar2 = Integer.valueOf(totalBayar);
                final int kembalian = bayarCondition3 - totalBayar2;

                if(!bayarCondition2.isEmpty()){
                    if(totalBayar2<bayarCondition3){
                        printDemo();

                    }else{
                        Toast.makeText(PesananDetailActivity.this,"Jumlah Nominal kurang !",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    etBayar.setError("Masukkan jumlah nominal");
                }

            }
        });
    }

    private void loadDetailPesanan() {
        final ProgressDialog progressDialog = new ProgressDialog(PesananDetailActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

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
                                progressDialog.dismiss();
                            }
                        }
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
                progressDialog.dismiss();
                recyclerView.setAdapter(adapter);
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", error.toString());
                progressDialog.dismiss();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    public void sum(){
        StringRequest jsonObjectRequest = new StringRequest(com.android.volley.Request.Method.GET,URL_SUM+"?atasnama="+atasnama, new Response.Listener<String>(){

            @Override
            public void onResponse(String response) {
                Log.e("Aaaa",response);
                try{
                    JSONObject jsonObject = new JSONObject(response);
                    int success = jsonObject.getInt("success");
                    String result = jsonObject.getString("result");

                    totalbayar.setText(IDUang.parsingRupiah(Integer.valueOf(result)) );

                }catch (JSONException e){
                    e.printStackTrace();
                }

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


    public void Bayar(){

        final String string = etBayar.getText().toString().trim();
        final String bayarCondition = string.replaceAll("[^0-9]", "");
        final String namakasir2 = namakasir.getText().toString().trim();
        final String date2 = date.getText().toString().trim();

        if(!bayarCondition.isEmpty()){
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_BAYAR,
                    new Response.Listener<String>(){
                        @Override
                        public void onResponse(String response) {
                            Log.e("response",response);
                            final String name = namakasir.getText().toString();

                            final String string = etBayar.getText().toString().trim();
                            final String bayarCondition2 = string.replaceAll("[^0-9]", "");
                            final String string1 = totalbayar.getText().toString().trim();
                            final String totalBayar = string1.replaceAll("[^0-9]", "");

                            int bayarCondition3 = Integer.valueOf(bayarCondition2);
                            int totalBayar2 = Integer.valueOf(totalBayar);
                            final int kembalian = bayarCondition3 - totalBayar2;

                            final String string2 = String.valueOf(kembalian);
                            final String kembalianS = string2.replaceAll("[^0-9]", "");

                            Intent intent = new Intent(PesananDetailActivity.this, Kembalian.class);
                            intent.putExtra("name",name);
                            intent.putExtra("kembalian",kembalianS);
                            intent.putExtra("tanggal",date.getText().toString());
                            intent.putExtra("totalbayar",totalBayar);
                            intent.putExtra("bayarcustomer",bayarCondition2);
                            intent.putExtra("atasnama",atasnama);
                            intent.putExtra("namakasir",namakasir2);
                            startActivity(intent);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(PesananDetailActivity.this,"Gagal melakukan pesanan "+error.toString(),Toast.LENGTH_SHORT).show();
                        }
                    }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    String nama = "",harga="",qty="",subtotal="";
                    for(int i=0;i<pesananList2.size();i++){
                        nama += pesananList2.get(i).getNama()+",";
                        harga += pesananList2.get(i).getHarga()+",";
                        qty += pesananList2.get(i).getQty()+",";
                        subtotal += pesananList2.get(i).getSubtotal()+",";
                    }
                    params.put("name", nama);
                    params.put("harga", harga);
                    params.put("qty", qty);
                    params.put("count",String.valueOf(pesananList2.size()));
                    params.put("subtotal", subtotal);
                    params.put("atasnama", atasnama);
                    params.put("namakasir", namakasir2);
                    params.put("tanggal", date2);
                    return params;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(PesananDetailActivity.this);
            requestQueue.add(stringRequest);
        }else{
            Toast.makeText(PesananDetailActivity.this,"Masukkan Nominal Bayar !",Toast.LENGTH_SHORT).show();
        }
    }

    protected void printDemo() {

        Intent intent = getIntent();
        final String extraNmae = intent.getStringExtra("name");

        final String string = etBayar.getText().toString().trim();
        final String bayarCondition2 = string.replaceAll("[^0-9]", "");

        final String string2 = totalbayar.getText().toString().trim();
        final String totalBayar = string2.replaceAll("[^0-9]", "");


        int bayarCondition3 = Integer.valueOf(bayarCondition2);
        int totalBayar2 = Integer.valueOf(totalBayar);

        int kembalian = bayarCondition3 - totalBayar2;


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
                printCustom(date.getText().toString() + "                             " + extraNmae, 0, 1);
                printCustom("__________________________________________", 0, 1);

                for (int i = 0; i < pesananList2.size(); i++) {
                    printCustom(pesananList2.get(i).getNama()+ "( "+ pesananList2.get(i).getKeterangan() + " )", 0, 0);
                    printCustom(pesananList2.get(i).getQty() + " X " + IDUang.parsingRupiah(Integer.parseInt(pesananList2.get(i).getHarga()))
                            + "                 " + IDUang.parsingRupiah(Integer.parseInt(pesananList2.get(i).getSubtotal())), 0, 0);
                }

                printCustom("__________________________________________", 0, 1);
                printCustom("Total                         " + string2, 0, 0);
                printCustom("Bayar(Cash)                   " + string, 0, 0);
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
            Bayar();
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

                final String string = etBayar.getText().toString().trim();
                final String bayarCondition3 = string.replaceAll("[^0-9]", "");
                printText(bayarCondition3);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
