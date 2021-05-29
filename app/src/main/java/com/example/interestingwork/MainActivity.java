package com.example.interestingwork;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.interestingwork.pager.PagerActivity;

public class MainActivity extends AppCompatActivity {
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayout = findViewById(R.id.buttonContainer);
        addButtons();
    }

    private void addButtons() {
        linearLayout.addView(createButton("pager的实现", PagerActivity.class));
    }

    private Button createButton(String text, final Class cls) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(20);
        button.setOnClickListener(new ButtonListener(this, cls));
        return button;
    }

    private class ButtonListener implements View.OnClickListener {
        private Context context;
        private Class cls;

        ButtonListener(Context context, Class cls) {
            this.context = context;
            this.cls = cls;
        }

        @Override
        public void onClick(View v) {
            Intent in = new Intent(context, cls);
            startActivity(in);
        }
    }
}