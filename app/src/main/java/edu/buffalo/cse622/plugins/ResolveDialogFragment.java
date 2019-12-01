/*
 * Copyright 2018 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.buffalo.cse622.plugins;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.ar.sceneform.ux.ArFragment;

/** A DialogFragment for the Resolve Dialog Box. */
public class ResolveDialogFragment extends DialogFragment {
  private static final String PTAG = "PartiksTag";
  Activity activity;
  Context context;
  ArFragment arFragment;
  RadioGroup objectsGroup;
  private OkListener okListener;

  // The maximum number of characters that can be entered in the EditText.
  private static final int MAX_FIELD_LENGTH = 6;

  /** Functional interface for getting the value entered in this DialogFragment. */
  public interface OkListener {
    /**
     * This method is called by the dialog box when its OK button is pressed.
     *
     * @param dialogValue the long value from the dialog box
     */
    void onOkPressed(String dialogValue);
  }

  public void partiksSetup(ArFragment arFragment2){
    this.activity = arFragment2.getActivity();
    this.context = arFragment2.getContext();
    this.arFragment = arFragment2;
  }

  public static ResolveDialogFragment createWithOkListener(OkListener listener) {
    Log.e("PartiksTag","createWithOkListener CALLED");
    ResolveDialogFragment frag = new ResolveDialogFragment();
    frag.okListener = listener;
    Log.e("PartiksTag","createWithOkListener END");
    return frag;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Log.e("PartiksTag","onCreateDialog CALLED");
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setView(createDialogLayout());
    builder.setTitle("Choose object to place");
    builder.setPositiveButton("OK", (dialog, which) -> onResolvePressed()  );
    builder.setNegativeButton("Cancel", (dialog, which) -> {} );
    /*AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder
        .setView(createDialogLayout())
        .setTitle("Resolve Anchor")
        .setPositiveButton("Resolve", (dialog, which) -> onResolvePressed())
        .setNegativeButton("Cancel", (dialog, which) -> {}); */
    Log.e("PartiksTag","onCreateDialog END");
    return builder.create();
  }

  private RadioGroup createDialogLayout() {
    Log.e("PartiksTag","createDialogLayout START");
    objectsGroup = new RadioGroup(context);
    objectsGroup.setOrientation(RadioGroup.VERTICAL);

    RadioButton pottedPlantOption = new RadioButton(context);
    pottedPlantOption.setId(View.generateViewId());
    pottedPlantOption.setText("Potted Plant");
    objectsGroup.addView(pottedPlantOption);

    RadioButton bedOption = new RadioButton(context);
    bedOption.setId(View.generateViewId());
    bedOption.setText("Bed");
    objectsGroup.addView(bedOption);

    RadioButton couchOption = new RadioButton(context);
    couchOption.setId(View.generateViewId());
    couchOption.setText("Couch");
    objectsGroup.addView(couchOption);

    RadioButton deskOption = new RadioButton(context);
    deskOption.setId(View.generateViewId());
    deskOption.setText("Desk");
    objectsGroup.addView(deskOption);

    RadioButton officeChairOption = new RadioButton(context);
    officeChairOption.setId(View.generateViewId());
    officeChairOption.setText("Office Chair");
    objectsGroup.addView(officeChairOption);
    /*Context context = getContext();
    LinearLayout layout = new LinearLayout(context);
    shortCodeField = new EditText(context);
    // Only allow numeric input.
    shortCodeField.setInputType(InputType.TYPE_CLASS_NUMBER);
    shortCodeField.setLayoutParams(
        new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    // Set a max length for the input text to avoid overflows when parsing.
    shortCodeField.setFilters(new InputFilter[] {new InputFilter.LengthFilter(MAX_FIELD_LENGTH)});
    layout.addView(shortCodeField);
    layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

    return layout; */
    Log.e("PartiksTag","createDialogLayout END");
    return objectsGroup;
  }

  private void onResolvePressed() {
    Log.e("PartiksTag","onResolvePressed START");
    int radioButtonID = objectsGroup.getCheckedRadioButtonId();
    View radioButtonView = objectsGroup.findViewById(radioButtonID);
    int selectedIndex = objectsGroup.indexOfChild(radioButtonView);

    RadioButton radioButton = (RadioButton) objectsGroup.getChildAt(selectedIndex);
    String objectChosen = radioButton.getText().toString();

    if(okListener != null){
      Log.e(PTAG, "OK LISTENER NOT NULL, CALLING LISTENER ! ");
      okListener.onOkPressed(objectChosen);
      Log.e(PTAG, "OK LISTENER CALLING ENDED");
    }
    Log.e(PTAG, "OK LISTENER CHECK FINISH");

    /*Editable roomCodeText = shortCodeField.getText();
    if (okListener != null && roomCodeText != null && roomCodeText.length() > 0) {
      int longVal = Integer.parseInt(roomCodeText.toString());
      okListener.onOkPressed(longVal);
    } */
    Log.e("PartiksTag","onResolvePressed END");
  } //onResolvePressed END

}
