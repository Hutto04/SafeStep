package com.example.myapplication.ui.notification;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.R;

import java.util.Properties;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NotificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationFragment extends Fragment {
    EditText _txtEmail, _txtMessage;
    Button _btnSend;

    public NotificationFragment() {}

    public static NotificationFragment newInstance() {
        NotificationFragment fragment = new NotificationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        _txtEmail = view.findViewById(R.id.txtEmail);
        _txtMessage = view.findViewById(R.id.txtMessage);
        _btnSend = view.findViewById(R.id.txtSend);

        _btnSend.setOnClickListener(v -> {
            Log.w("NotificationStart","Starting App..");
            final String username="spiderknight22@gmail.com"; // need to put gmail email inside
            final String password="szgx muhb zpqi fkfo"; // need to put app password for gmail
            String messageToSend=_txtMessage.getText().toString();
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "465");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

            Session session = Session.getInstance(props, new javax.mail.Authenticator(){
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            Log.w("NotificationPass","Pass Created");
            try{
                session.setDebug(true);
                Log.w("NotificationPass","In Try..");
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                String mail = _txtEmail.getText().toString();
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail));
                message.setSubject("Safe-Step-Notification");
                message.setText(messageToSend);
                Transport.send(message);
                Toast.makeText(getContext(), "email sent successfully", Toast.LENGTH_LONG).show();
            }catch(MessagingException e){
                Log.w("NotificationCatch","In catch");
                throw new RuntimeException(e);

            }
        });

        return view;
    }
}