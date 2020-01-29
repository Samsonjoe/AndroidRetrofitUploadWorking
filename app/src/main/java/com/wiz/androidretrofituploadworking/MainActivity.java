package com.wiz.androidretrofituploadworking;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.wiz.androidretrofituploadworking.Remote.IUploadAPI;
import com.wiz.androidretrofituploadworking.Remote.RetrofitClient;
import com.wiz.androidretrofituploadworking.Utils.Permissions;
import com.wiz.androidretrofituploadworking.Utils.ProgressRequestBody;
import com.wiz.androidretrofituploadworking.Utils.UploadcallBacks;

import java.io.File;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements UploadcallBacks {

    private static final String TAG = "MainActivity";
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;


    //FOR USING ON VIRTUAL PHONE ON PC
  //  public static final String BASE_URL = "http://10.0.2.2/";

   // public static final String BASE_URL = "https://collaborationkenya.minet.com/MinetAPI/tsc/";
   public static final String BASE_URL ="https://minetkenya.minet.com/minetapi/v1/tsc/";

    private static final int REQUEST_PERMISSION = 1000;
    private static final int PICK_FILE_REQUEST = 1001 ;

    IUploadAPI mService;

    Button btnUpload;
    ImageView imageView;

    Uri selectedFileUri;

    ProgressDialog dialog;

    private IUploadAPI getAPIUpload()
    {
        return RetrofitClient.getClient(BASE_URL).create(IUploadAPI.class);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check permission
     /*   if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
        {
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            },REQUEST_PERMISSION);
        }*/

        if(checkPermissionArray(Permissions.PERMISSIONS)){

        }else{
            verifyPermissions(Permissions.PERMISSIONS);
        }


        //Service
        mService = getAPIUpload();

        //Init view
        btnUpload = (Button)  findViewById(R.id.btn_upload);
        imageView = (ImageView)  findViewById(R.id.image_view);

        //Event
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseFile();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();

            }
        });
    }


    /***
     * verify all the permissions passed to the array
     * @param permissions
     */
    private void verifyPermissions(String[] permissions) {
        Log.d(TAG, "verifyPermissions: verifying permissions.");

        ActivityCompat.requestPermissions(
                MainActivity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }


    /**
     * Check an array of permissions
     * @param permissions
     * @return
     */

    public boolean checkPermissionArray(String[] permissions) {
        Log.d(TAG, "checkPermissionArray: checking permissions array.");

        for (int i = 0; i<permissions.length; i++){
            String check = permissions[i];
            if(!checkPermissions(check)){
                return false;
            }
        }
        return true;
    }

    /**
     * Check a single permission it has been verified
     * @param permission
     * @return
     */
    public boolean checkPermissions(String permission) {
        Log.d(TAG, "checkPermissions: checking permission " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(MainActivity.this, permission);

        if(permissionRequest != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermissions: \n Permission wa not granted for: " + permission);
            return false;
        }
        else{
            Log.d(TAG, "checkPermissions: \n permission was granted for: " + permission);
            return true;
        }
    }


    private void uploadFile() {
            if (selectedFileUri != null)
            {
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setMessage("Uploading....");
                dialog.setIndeterminate(false);
                dialog.setMax(100);
                dialog.setCancelable(false);
                dialog.show();

                File file = FileUtils.getFile(this,selectedFileUri);
                ProgressRequestBody requestFile = new ProgressRequestBody(file,this);

                final MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file",file.getName(),requestFile);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mService.uploadFile(body)
                                .enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        dialog.dismiss();
                                        Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {
                                        dialog.dismiss();
                                        Toast.makeText(MainActivity.this,t.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }
                }).start();
            }

        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_PERMISSION:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /* @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Activity.RESULT_OK)
        {
            if(requestCode == PICK_FILE_REQUEST)
            {
                if (data != null)
                {
                    selectedFileUri = data.getData();
                    if(selectedFileUri != null && !selectedFileUri.getPath().isEmpty())
                        imageView.setImageURI(selectedFileUri);
                    else
                        Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();

                }
            }
        }
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub


        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            Uri selectedImmage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImmage, filePathColumn, null, null, null);
            // columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String uploadFileName = cursor.getString(column_index);
            // Log.e("Attachment Path:",uploadFileName);

            selectedFileUri = Uri.parse("file://" + uploadFileName);
            cursor.close();

            if(selectedFileUri != null && !selectedFileUri.getPath().isEmpty()) {
                imageView.setImageURI(selectedFileUri);
                Toast.makeText(MainActivity.this, uploadFileName, Toast.LENGTH_LONG).show();
                btnUpload.setEnabled(true);
            }
            else{
                Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
                btnUpload.setEnabled(false);
            }


            //uploadFileName =  data.getData().getPath();
            // uploadFilePath = data.getData().getPath();


        }
    }

    private void chooseFile() {

            Intent intent = Intent.createChooser(FileUtils.createGetContentIntent(),"Select a file");
            startActivityForResult(intent,PICK_FILE_REQUEST);
        }

    @Override
    public void onProgressUpdate(int percentage) {
        dialog.setProgress(percentage);
    }
}
