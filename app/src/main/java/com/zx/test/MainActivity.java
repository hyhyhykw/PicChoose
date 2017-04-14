package com.zx.test;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String FYAPKTEST = "";

    private String result;
    //用于展示选择的图片
    private ImageView mImageView;

    private ImageView mImgShow2;

    //    //pipei
    private Button mBtnMatch;
    //    //jisuan
    private Button mBtnCal;

    private Button mBtn;

    private RadioGroup mGroup;
    private boolean isFromCamera;

    //请求码：相机=1 相册=2  裁剪=3
    private static final int CAMERA_CODE = 10;
    private static final int GALLERY_CODE = 20;
//    private static final int CROP_CODE = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    //初始化
    private void init() {

        mGroup = (RadioGroup) findViewById(R.id.group);

        mGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.rbtn_camera://相机
                        isFromCamera = true;
                        break;
                    case R.id.rbtn_gallery:
                        isFromCamera = false;
                        break;
                }
            }
        });

        mImageView = (ImageView) findViewById(R.id.show_image);
        mImgShow2 = (ImageView) findViewById(R.id.show_image2);

        // TODO: 2017/4/14
        mImageView.setOnClickListener(this);
        mImgShow2.setOnClickListener(this);


        mBtnMatch = (Button) findViewById(R.id.btn_match);
        mBtnMatch.setOnClickListener(this);
        mBtnCal = (Button) findViewById(R.id.btn_cal);
        mBtnCal.setOnClickListener(this);
        mBtn = (Button) findViewById(R.id.btn);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == mImageView.getTag() || null == mImgShow2.getTag()) {
                    Toast.makeText(MainActivity.this, "请选择正确的图片", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.e("TAG", mImageView.getTag().toString());
                Log.e("TAG", mImgShow2.getTag().toString());
            }
        });
    }

    //图片转化成base64字符串
    public static String getImageStr(String path) {//将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        InputStream in;
        byte[] data = null;
        //读取图片字节数组
        try {
            in = new FileInputStream(path);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //对字节数组Base64编码
        return Base64.encodeToString(data, Base64.DEFAULT);//返回Base64编码过的字节数组字符串
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_image:
                if (isFromCamera) {
                    chooseFromCamera(1);
                } else {
                    chooseFromGallery(1);
                }
                break;
            case R.id.show_image2:
                if (isFromCamera) {
                    chooseFromCamera(2);
                } else {
                    chooseFromGallery(2);
                }
                break;
            case R.id.btn_match://匹配
                new Thread() {
                    @Override
                    public void run() {
                        String path1 = (String) mImageView.getTag();
                        String path2 = (String) mImgShow2.getTag();
                        if (null == path1 || null == path2) return;

                        String str1 = getImageStr(path1);
                        String str2 = getImageStr(path2);


                        getData(str1, str2);
                    }
                }.start();
                break;
            case R.id.btn_cal://计算
                break;
            default:
                break;
        }
    }


    private void chooseFromGallery(int index) {//从相册选取
        //构建一个内容选择的Intent
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //设置选择类型为图片类型
        intent.setType("image/*");
        //打开图片选择
        startActivityForResult(intent, GALLERY_CODE + index);
    }

    /**
     * 拍照选择，这里可能会有权限问题闪退
     * 出现异常告诉我我再看看，没出现就不管了
     */
    private void chooseFromCamera(int index) {//拍照选择
        //构建隐式Intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //调用系统相机
        startActivityForResult(intent, CAMERA_CODE + index);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**
         * case 11://第一个 相机

         //                break;
         //            case 21://第一个 相册
         //                break;
         //            case 12://第二个 相机
         //                break;
         //            case 22://第二个 相册
         */
//        if (resultCode != RESULT_OK) return;

//        Log.e("TAG",""+resultCode);
        if (requestCode == 11 || requestCode == 12) {
            if (data == null) {
                return;
            } else {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    //获得拍的照片
                    Bitmap bm = extras.getParcelable("data");
                    //将Bitmap转化为uri
                    Uri uri = saveBitmap(bm, "temp");

                    if (null != uri) {
                        if (requestCode == 11) {
                            mImageView.setImageBitmap(bm);
                            mImageView.setTag(uri.getPath());
                        } else {
                            mImgShow2.setImageBitmap(bm);
                            mImgShow2.setTag(uri.getPath());
                        }
                    }
                }
            }
        } else if (requestCode == 21 || requestCode == 22) {
            if (data == null) {
                return;
            } else {
                //用户从图库选择图片后会返回所选图片的Uri
                Uri uri;
                //获取到用户所选图片的Uri
                uri = data.getData();
                //返回的Uri为content类型的Uri,不能进行复制等操作,需要转换为文件Uri
                uri = convertUri(uri);
                if (null != uri) {
//                        String path = uri.getPath();
                    if (requestCode == 21) {
                        mImageView.setImageURI(uri);
                        mImageView.setTag(uri.getPath());
                        Log.e("TAG", uri.getPath());
                    } else {
                        mImgShow2.setImageURI(uri);
                        mImgShow2.setTag(uri.getPath());
                        Log.e("TAG", uri.getPath());
                    }
                }
//                    startImageZoom(uri);
            }
        }

    }

    /**
     * 将content类型的Uri转化为文件类型的Uri
     *
     * @param uri
     * @return
     */
    private Uri convertUri(Uri uri) {
        InputStream is;
        try {
            //Uri ----> InputStream
            is = getContentResolver().openInputStream(uri);
            //InputStream ----> Bitmap
            Bitmap bm = BitmapFactory.decodeStream(is);
            //关闭流
            is.close();
            return saveBitmap(bm, "temp");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将Bitmap写入SD卡中的一个文件中,并返回写入文件的Uri
     *
     * @param bm
     * @param dirPath
     * @return
     */
    private Uri saveBitmap(Bitmap bm, String dirPath) {
        //新建文件夹用于存放裁剪后的图片
        File tmpDir = new File(Environment.getExternalStorageDirectory() + "/" + dirPath);
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }

        //新建文件存储裁剪后的图片
        Date date = new Date();
        File img = new File(tmpDir.getAbsolutePath() + "/avator_" + date.getTime() + ".png");
        try {
            //打开文件输出流
            FileOutputStream fos = new FileOutputStream(img);
            //将bitmap压缩后写入输出流(参数依次为图片格式、图片质量和输出流)
            bm.compress(Bitmap.CompressFormat.PNG, 85, fos);
            //刷新输出流
            fos.flush();
            //关闭输出流
            fos.close();
            //返回File类型的Uri
            return Uri.fromFile(img);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    //$################################################################

    public void getData(String str1, String str2) {
        // 命名空间
        String targetNamespace = "http://itfc.eway.com/";
        // 调用的方法名称
        String element = "tns:matchPhoto";
        // EndPoint
        String endPoint = "http://101.200.160.247:5653/eway/UtilImplPort";
        // SOAP Action
        String soapAction = "http://itfc.eway.com/tns:matchPhoto";

        // 指定WebService的命名空间和调用的方法名
        SoapObject rpc = new SoapObject(targetNamespace, element);

        // 设置需调用WebService接口需要传入的三个个参数arg0、arg1、arg2
        rpc.addProperty("arg0", str1);
        rpc.addProperty("arg1", str2);
        rpc.addProperty("arg2", FYAPKTEST);

        // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);

        envelope.bodyOut = rpc;
        // 设置是否调用的是dotNet开发的WebService
        envelope.dotNet = true;
        // 等价于envelope.bodyOut = rpc;
        envelope.setOutputSoapObject(rpc);

        HttpTransportSE transport = new HttpTransportSE(endPoint);
        try {
            // 调用WebService
            transport.call(soapAction, envelope);

            // 获取返回的数据
//            SoapObject object = (SoapObject) envelope.bodyIn;

            SoapObject object = (SoapObject) envelope.getResponse();
            // 获取返回的结果
            result = object.getProperty(0).toString();
            showNormalDialog(result);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void showNormalDialog(String result) {
        /*
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MainActivity.this);
        normalDialog.setTitle("比对结果：");
        normalDialog.setMessage(result);
        normalDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO: 2017/4/12
                    }
                });
        // 显示
        normalDialog.show();
    }

}
