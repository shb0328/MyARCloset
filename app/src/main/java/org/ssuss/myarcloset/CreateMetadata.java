package org.ssuss.myarcloset;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class CreateMetadata extends Activity {

    private RadioGroup radioGroup;
    private Button btn;
    private String TopOrBottomMetadata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_create_metadata);


        radioGroup = findViewById(R.id.topOrBottom);


        btn = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int id = radioGroup.getCheckedRadioButtonId();
                TopOrBottomMetadata = ((RadioButton)findViewById(id)).getText().toString();

                Intent intent = new Intent();
                intent.putExtra("classification",TopOrBottomMetadata);
//                setResult(RESULT_OK,intent);
                finish();
            }
        });

    }


    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()== MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

}
