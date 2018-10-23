package tracebrace.tracebrace_final;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.security.Permission;
import java.util.ArrayList;

public class MessagesActivity extends AppCompatActivity {

    LinearLayout mainLayout;
    Button addMessage;
    Button settings;
    EditText message;
    EditText number;
    View view;
    View view2;

    Button confirm;
    Button cancel;

    int k;

    EditText messageText;
    EditText numberText;

    TinyDB localDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);


        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
            System.out.println("sHOULD START");
            if(!isMyServiceRunning(BackgroundService.class)){
                startService(new Intent(getApplicationContext(), BackgroundService.class));
                System.out.println("Start");
            }
        }


        addMessage = findViewById(R.id.addButton);

        localDb = new TinyDB(this);
        settings = findViewById(R.id.settingsButton);

        mainLayout = findViewById(R.id.messagesLayout);
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;

        final LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams userPhotoParams = new LinearLayout.LayoutParams((int)(70*scale+0.5f), (int)(70*scale+0.5f));
        LinearLayout.LayoutParams viewPortParams = new LinearLayout.LayoutParams((int)(3*scale+0.5f),(int)(3*scale+0.5f));
        TextView noMessages = new TextView(this);

        if(localDb.getInt("messagesCount")==0){

            noMessages.setText(R.string.no_messages);
            noMessages.setTextSize((int)(20*scale+0.5f));
            noMessages.setGravity(Gravity.CENTER);
            mainLayout.addView(noMessages);

        }



        for(int  i=0; i<localDb.getInt("messageCount"); i++){
            mainLayout.removeView(noMessages);

            final CardView message = new CardView(getApplicationContext());
            message.setId(i);
            message.setLayoutParams(messageParams);
            message.setPadding(0,0,0,0);
            message.setCardElevation(4);
            ////////////
            k=i;
            message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final LayoutInflater inflater = LayoutInflater.from(MessagesActivity.this);
                    view2 = inflater.inflate(R.layout.edit_message, null);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MessagesActivity.this);
                    numberText = view2.findViewById(R.id.numberInput);
                    messageText = view2.findViewById(R.id.messageInput);

                    numberText.setText(localDb.getListString("numbers").get(message.getId()));
                    messageText.setText(localDb.getListString("messages").get(message.getId()));

                    builder.setView(view2);
                    builder.setCancelable(false);

                    builder.setPositiveButton(R.string.option_change, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ArrayList<String> numbers = localDb.getListString("numbers");
                            numbers.set(message.getId(), numberText.getText().toString());
                            localDb.putListString("numbers",numbers);


                            ArrayList<String> messages = localDb.getListString("messages");
                            messages.set(message.getId(), messageText.getText().toString());
                            localDb.putListString("messages",messages);

                            mainLayout.invalidate();
                            recreate();
                        }
                    });
                    builder.setNegativeButton(R.string.option_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    builder.setNeutralButton(R.string.option_delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ArrayList<String> numbers = localDb.getListString("numbers");
                            numbers.remove(message.getId());
                            localDb.putListString("numbers",numbers);


                            ArrayList<String> messages = localDb.getListString("messages");
                            messages.remove(message.getId());
                            localDb.putListString("messages",messages);

                            localDb.putInt("messageCount", localDb.getInt("messageCount")-1);
                            mainLayout.invalidate();
                            recreate();

                        }
                    });
                    builder.show();

                }
            });
            ///////////
            LinearLayout frame = new LinearLayout(getApplicationContext());
            frame.setOrientation(LinearLayout.HORIZONTAL);


            ImageView userPhoto = new ImageView(this);
            userPhoto.setLayoutParams(userPhotoParams);
            userPhoto.setPadding((int)(5*scale+0.5f),(int)(5*scale+0.5f),(int)(5*scale+0.5f),(int)(5*scale+0.5f));
            userPhoto.setImageResource(R.drawable.ic_user);

            frame.addView(userPhoto);


            LinearLayout textLayout = new LinearLayout(this);
            textLayout.setOrientation(LinearLayout.VERTICAL);

            TextView number = new TextView(getApplicationContext());
            //number.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            number.setText(localDb.getListString("numbers").get(i));
            number.setTextSize((int)(10*scale+0.5f));
            number.setPadding((int)(3*scale+0.5f),(int)(8*scale+0.5f),0,0);

            textLayout.addView(number);

            TextView messageContent = new TextView(this);
            messageContent.setText(localDb.getListString("messages").get(i));
            messageContent.setTextSize((int)(8*scale+0.5f));
            messageContent.setPadding((int)(3*scale+0.5f),(int)(5*scale+0.5f),0,0);

            textLayout.addView(messageContent);

            frame.addView(textLayout);

            message.addView(frame);

            View viewPort = new View(this);

            viewPort.setLayoutParams(viewPortParams);

            mainLayout.addView(message);
            mainLayout.addView(viewPort);
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////////////

        final LayoutInflater inflater = LayoutInflater.from(MessagesActivity.this);


        addMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view = inflater.inflate(R.layout.new_message, null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(MessagesActivity.this);
                number = view.findViewById(R.id.numberInput);
                message = view.findViewById(R.id.messageInput);

                builder.setView(view);
                builder.setCancelable(false);

                builder.setPositiveButton(R.string.option_add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<String> numbers = localDb.getListString("numbers");
                        numbers.add(number.getText().toString());
                        localDb.putListString("numbers",numbers);


                        ArrayList<String> messages = localDb.getListString("messages");
                        messages.add(message.getText().toString());
                        localDb.putListString("messages",messages);

                        localDb.putInt("messageCount", localDb.getInt("messageCount")+1);

                        mainLayout.invalidate();
                        recreate();
                    }
                });
                builder.setNegativeButton(R.string.option_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });

    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            sharedPref.edit().putBoolean("gps_allow", false).apply();

        } else {
            if(!isMyServiceRunning(BackgroundService.class)){
                startService(new Intent(getApplicationContext(), BackgroundService.class));
                System.out.println("Start");
            }
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            System.out.println("Camera allowd");
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED) {
            sharedPref.edit().putBoolean("gps_allow", false).apply();
        }
    }





}
