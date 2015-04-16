package com.vodafone.mycomms.custom;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.vodafone.mycomms.R;

/**
 * Created by str_rbm on 14/04/2015.
 */
public class AutoCompleteTVSelectOnly extends AutoCompleteTextView {

    int position;
    private int[] views;
    String textSelected = null;

    public int getPosition(){return position;}

    public void setPosition(int position){this.position = position;}

    public AutoCompleteTVSelectOnly(Context context) {
        super(context);
        init();
    }

    public AutoCompleteTVSelectOnly(Context context, AttributeSet attrs)
    {
        super(context,attrs);
        init();
    }

    public AutoCompleteTVSelectOnly(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context,attrs,defStyleAttr);
        init();
    }

    @Override
    public boolean enoughToFilter() {
        if(length()>0) return true;
        else return false;
    }

    private void init()
    {
        position = -1;

        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                setPosition(position);
                setError(null,null);
                String text = "";

                for(int i=0; i<views.length; i++) {
                    if(i>0) text += " ";
                    text += ((TextView) view.findViewById(views[i])).getText().toString();
                }

                setText(text);
                textSelected = text;
            }
        });

        addTextChangedListener(new TextWatcher() {
            @Override
              public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(textSelected != null && getText().toString().compareTo(textSelected) != 0)
                    position = -1;
            }
        });
    }

    public int[] getViews() {
        return views;
    }

    public void setViews(int[] views) {
        this.views = views;
    }
}
