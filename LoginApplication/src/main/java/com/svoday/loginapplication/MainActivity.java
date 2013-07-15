package com.svoday.loginapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private TextView mTextView01;
    private LayoutInflater mInflater01;
    private View mView01;
    private EditText mEditText01,mEditText02;
    private String TAG = "HTTP_DEBUG";
    /*汉字的距离*/
    private int intShiftPadding = 14;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*创建DisplayMetrics对象，取得屏幕分辨率*/
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        mTextView01 = (TextView)findViewById(R.id.myTextView1);

        /*将文字lael放在屏幕右上方*/
//        mTextView01.setLayoutParams(
//            new AbsoluteLayout.LayoutParams(
//                    intShiftPadding*mTextView01.getText().toString().length(),18,
//                    (
//                            dm.widthPixels-(
//                                        intShiftPadding*mTextView01.getText().toString().length()
//                            ))-10,0
//                            )
//        );

        ConnectivityManager cwjManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cwjManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()){
            Toast.makeText(MainActivity.this,"有互联网连接",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(MainActivity.this,"无互联网连接",Toast.LENGTH_SHORT).show();
        }

        mTextView01.setOnClickListener(new TextView.OnClickListener(){
            @Override
            public void onClick(View view) {
                showLoginForm();
            }
        }
        );
    }

    private void showLoginForm()
    {
        try
        {
            /*以LayoutInflater取得主Activity的context*/
            mInflater01 = LayoutInflater.from(MainActivity.this);
            /*设置创建的View所要使用的Layout Resource */
            mView01 = mInflater01.inflate(R.layout.login,null);
            /*账号*/
            mEditText01 = (EditText) mView01.findViewById(R.id.myEditText1);
            /*密码*/
            mEditText02 = (EditText) mView01.findViewById(R.id.myEditText2);

            /*建立Login窗体对话框*/
            new AlertDialog.Builder(this)
                    .setView(mView01)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                /*当按下ok按钮进行登录网络操作*/
                                public void onClick
                                (DialogInterface dialog, int whichButton) {
                            /*调用自定义processInternetLogin函数登陆*/
                                    if (processInternetLogin(mEditText01.getText().toString(), mEditText02.getText().toString())) {
                                        /*若登陆成功，则结束此Activity调到登陆成功页面*/
                                        Intent i = new Intent();
                                        i.setClass(MainActivity.this, SecondActivity.class);
                                        MainActivity.this.startActivity(i);
                                        finish();//结束当前Activity
                                    } else {
                                        Toast.makeText(MainActivity.this, "账号或密码错误", Toast.LENGTH_SHORT).show();
                                    }


                                }
                            }).show();
        }
            catch(Exception e)
            {
                e.printStackTrace();
            }
    }

    /*自定义登陆网站URL Login操作*/
    private boolean processInternetLogin(String strUID,String strUPW)
    {
//      /*
//      * Demo登陆
//      * 账号:david
//      * 密码:1234
//      */
        MyThread myThread = new MyThread(strUID,strUPW);
        myThread.start();
        while(myThread.isAlive()){
            try{
                Thread.sleep(100);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        System.out.println("在主函数 Thread NO." + Thread.currentThread().getId() + " 连接状态 " + myThread.status);
        return myThread.status;
    }

    class MyThread extends Thread{
        private  String strUID,strUPW;

        public MyThread(String strUID,String strUPW){
            this.strUID = strUID;
            this.strUPW = strUPW;
        }
        public boolean status = false;
        @Override
        public void run() {
            String uriAPI = "https://day961.uqute.com/API/Login/index.php";
            String strRet = "";

            try
            {
                Looper.prepare();
                System.out.println("进入判断函数   Thread NO." + Thread.currentThread().getId());



                DefaultHttpClient httpclient = (DefaultHttpClient)getNewHttpClient();
                //DefaultHttpClient httpclient = new DefaultHttpClient();
                
                HttpResponse response;
                HttpPost httpPost = new HttpPost(uriAPI);
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                nvps.add(new BasicNameValuePair("uid",strUID));
                nvps.add(new BasicNameValuePair("upw",strUPW));

                HttpEntity httpentity = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
                httpPost.setEntity(httpentity);

                response = httpclient.execute(httpPost);

                /*HttpStatus.SC_OK表示连接成功*/
                if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
                {
                    System.out.println("请求成功   Thread NO." + Thread.currentThread().getId());
                }else{
                    System.out.println("请求错误   Thread NO." + Thread.currentThread().getId());
                }

                HttpEntity entity = response.getEntity();

                Log.d(TAG,"HTTP POST getStatusLine:" + response.getStatusLine());

                strRet = EntityUtils.toString(entity);
                System.out.println("返回内容  " + strRet);
                Log.i(TAG,strRet);
                strRet = strRet.trim().toLowerCase();//trim清除空格

                /*取得Cookie内容*/
                List<Cookie> cookies = httpclient.getCookieStore().getCookies();

                if(entity != null)
                {
                    entity.consumeContent();
                }

                Log.d(TAG,"HTTP POST Initialize of cookies.");
                cookies = httpclient.getCookieStore().getCookies();
                if (cookies.isEmpty())
                {
                    Log.d(TAG,"HTTP POST Cookie not found.");
                    Log.i(TAG,entity.toString());
                }
                else
                {
                    for (int i = 0;i < cookies.size();i++)
                    {
                        Log.d(TAG,"HTTP POST Found Cookie:" + cookies.get(i).toString());
                    }
                }
                if (strRet.equals("y"))
                {
                    Log.i("TEST","YES");
                    status = true;
                    System.out.println("账号正确   Thread NO." + Thread.currentThread().getId());
                }
                else
                {
                    Log.i("TEST","NO");
                    status = false;
                    System.out.println("账号错误   Thread NO." + Thread.currentThread().getId());
                }
                stop();//强制线程退出
                Looper.loop();
                //super.run();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.out.println("进入异常   Thread NO." + Thread.currentThread().getId() + e.getMessage());
                //return false;
            }

            //super.run();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public static HttpClient getNewHttpClient()  {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    String requestHTTPSPage(String mUrl) {
        InputStream ins = null;
        String result = "";
        try {
            ins = MainActivity.this.getAssets().open("day961.cer"); //下载的证书放到项目中的assets目录中
            CertificateFactory cerFactory = CertificateFactory
                    .getInstance("X.509");
            Certificate cer = cerFactory.generateCertificate(ins);
            KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
            keyStore.load(null, null);
            keyStore.setCertificateEntry("trust", cer);

            SSLSocketFactory socketFactory = new SSLSocketFactory(keyStore);
            Scheme sch = new Scheme("https", socketFactory, 443);
            HttpClient mHttpClient = new DefaultHttpClient();
            mHttpClient.getConnectionManager().getSchemeRegistry()
                    .register(sch);

            BufferedReader reader = null;
            try {
                Log.d(TAG, "executeGet is in,murl:" + mUrl);
                HttpGet request = new HttpGet();
                request.setURI(new URI(mUrl));
                HttpResponse response = mHttpClient.execute(request);
                if (response.getStatusLine().getStatusCode() != 200) {
                    request.abort();
                    return result;
                }

                reader = new BufferedReader(new InputStreamReader(response
                        .getEntity().getContent()));
                StringBuffer buffer = new StringBuffer();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                result = buffer.toString();
                Log.d(TAG, "mUrl=" + mUrl + "\nresult = " + result);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            try {
                if (ins != null)
                    ins.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
