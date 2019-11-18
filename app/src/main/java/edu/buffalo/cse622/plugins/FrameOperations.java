package edu.buffalo.cse622.plugins;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.SkeletonNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FrameOperations {

    private Resources dynamicResources;
    private ArFragment arFragment;
    private Context context;

    private ViewRenderable pottedPlantTextRenderable;
    private ModelRenderable pottedPlantRenderable;
    private TransformableNode pottedPlantNode;

    private ModelRenderable bedRenderable;
    private TransformableNode bedNode;

    private ModelRenderable couchRenderable;
    private TransformableNode couchNode;

    private ModelRenderable deskRenderable;
    private TransformableNode deskNode;

    private ModelRenderable officeChairRenderable;
    private TransformableNode officeChairNode;

    /**
     * Constructor does all the resources loading that the plugin requires.
     *
     * @param dynamicResources The Resources object is already initialized and passed by MetaApp which helps the plugin to be "aware" of its own resources.
     * @param arFragment       ArFragment object passed by MetaApp.
     */
    public FrameOperations(Resources dynamicResources, ArFragment arFragment) {
        this.dynamicResources = dynamicResources;
        this.arFragment = arFragment;
        this.context = arFragment.getContext();

        // This is how we load a layout resource.
        int pottedPlantTextLayoutId = dynamicResources.getIdentifier("text_view", "layout", "edu.buffalo.cse622.plugins");
        XmlResourceParser pottedPlantTextViewXml = dynamicResources.getLayout(pottedPlantTextLayoutId);
        View pottedPlantTextView = LayoutInflater.from(context).inflate(pottedPlantTextViewXml, null);

        ViewRenderable.builder()
                .setView(context, pottedPlantTextView)
                .build()
                .thenAccept(
                        (renderable) -> {
                            pottedPlantTextRenderable = renderable;
                        });

        // This is how we load a model/asset.
        CompletableFuture<ModelRenderable> pottedPlantStage =
                ModelRenderable.builder().setSource(context, new Callable<InputStream>() {
                    @Override
                    public InputStream call() throws Exception {
                        InputStream inputStream = null;
                        try {
                            AssetManager assetManager = dynamicResources.getAssets();
                            inputStream = assetManager.open("potted_plant.sfb");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return inputStream;
                    }
                }).build();
        CompletableFuture<ModelRenderable> bedStage =
                ModelRenderable.builder().setSource(context, new Callable<InputStream>() {
                    @Override
                    public InputStream call() throws Exception {
                        InputStream inputStream = null;
                        try {
                            AssetManager assetManager = dynamicResources.getAssets();
                            inputStream = assetManager.open("bed.sfb");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return inputStream;
                    }
                }).build();
        CompletableFuture<ModelRenderable> couchStage =
                ModelRenderable.builder().setSource(context, new Callable<InputStream>() {
                    @Override
                    public InputStream call() throws Exception {
                        InputStream inputStream = null;
                        try {
                            AssetManager assetManager = dynamicResources.getAssets();
                            inputStream = assetManager.open("couch.sfb");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return inputStream;
                    }
                }).build();
        CompletableFuture<ModelRenderable> deskStage =
                ModelRenderable.builder().setSource(context, new Callable<InputStream>() {
                    @Override
                    public InputStream call() throws Exception {
                        InputStream inputStream = null;
                        try {
                            AssetManager assetManager = dynamicResources.getAssets();
                            inputStream = assetManager.open("desk.sfb");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return inputStream;
                    }
                }).build();
        CompletableFuture<ModelRenderable> officeChairStage =
                ModelRenderable.builder().setSource(context, new Callable<InputStream>() {
                    @Override
                    public InputStream call() throws Exception {
                        InputStream inputStream = null;
                        try {
                            AssetManager assetManager = dynamicResources.getAssets();
                            inputStream = assetManager.open("office_chair.sfb");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return inputStream;
                    }
                }).build();

        CompletableFuture.allOf(
                pottedPlantStage,
                bedStage,
                couchStage,
                deskStage,
                officeChairStage)
                .handle(
                        (notUsed, throwable) -> {
                            // When you build a Renderable, Sceneform loads its resources in the background while
                            // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
                            // before calling get().

                            if (throwable != null) {
                                Log.d("handle", "Unable to load renderable", throwable);
                                return null;
                            }

                            try {
                                pottedPlantRenderable = pottedPlantStage.get();
                                bedRenderable = bedStage.get();
                                couchRenderable = couchStage.get();
                                deskRenderable = deskStage.get();
                                officeChairRenderable = officeChairStage.get();
                            } catch (InterruptedException | ExecutionException ex) {
                                Log.d("handle", "Unable to load renderable", ex);
                            }

                            return null;
                        });

        pottedPlantNode = new TransformableNode(arFragment.getTransformationSystem());
        bedNode = new TransformableNode(arFragment.getTransformationSystem());
        couchNode = new TransformableNode(arFragment.getTransformationSystem());
        deskNode = new TransformableNode(arFragment.getTransformationSystem());
        officeChairNode = new TransformableNode(arFragment.getTransformationSystem());
    }

    /**
     * This is where we do most of our operations on the frame and return the AnchorNode object back to MetaApp.
     *
     * @param frame
     * @return
     */
    private void processFrame(Frame frame) {
    }

    private void planeTap(HitResult hitResult) {
        // Creates a popup with the list of objects that can be rendered
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose object to place");

        RadioGroup objectsGroup = new RadioGroup(context);
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

        builder.setView(objectsGroup);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int radioButtonID = objectsGroup.getCheckedRadioButtonId();
                View radioButtonView = objectsGroup.findViewById(radioButtonID);
                int selectedIndex = objectsGroup.indexOfChild(radioButtonView);

                RadioButton radioButton = (RadioButton) objectsGroup.getChildAt(selectedIndex);
                String objectChosen = radioButton.getText().toString();

                switch (objectChosen) {
                    case "Potted Plant":
                        renderPottedPlant(hitResult).setParent(arFragment.getArSceneView().getScene());

                        break;

                    case "Bed":
                        renderBed(hitResult).setParent(arFragment.getArSceneView().getScene());

                        break;

                    case "Couch":
                        renderCouch(hitResult).setParent(arFragment.getArSceneView().getScene());

                        break;

                    case "Desk":
                        renderDesk(hitResult).setParent(arFragment.getArSceneView().getScene());

                        break;

                    case "Office Chair":
                        renderOfficeChair(hitResult).setParent(arFragment.getArSceneView().getScene());

                        break;
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /*
     * Separate methods for object rendering in case we want to do any object specific configuration later.
     */

    private AnchorNode renderPottedPlant(HitResult hitResult) {
        AnchorNode anchorNode = null;

        if (pottedPlantRenderable != null && pottedPlantTextRenderable != null) {
            Anchor anchor = hitResult.createAnchor();
            anchorNode = new AnchorNode(anchor);

            // Create potted plant relative to anchor node
            pottedPlantNode.setParent(anchorNode);
            SkeletonNode pottedPlant = new SkeletonNode();
            pottedPlant.setParent(pottedPlantNode);
            pottedPlant.setRenderable(pottedPlantRenderable);
            pottedPlant.setLocalScale(new Vector3(0.25f, 0.25f, 0.25f));

            // Attach bone node
            Node boneNode = new Node();
            boneNode.setParent(pottedPlant);
            pottedPlant.setBoneAttachment("Potted Plant", boneNode);

            // Use bone node to display ViewRenderable
            Node pottedPlantTextNode = new Node();
            pottedPlantTextNode.setRenderable(pottedPlantTextRenderable);
            pottedPlantTextNode.setParent(boneNode);

            // Adjustments to the text position relative to the potted plant
            Vector3 pottedUp = pottedPlant.getUp();
            pottedUp.z += .4f;
            pottedUp.y += 2f;
            pottedPlantTextNode.setLocalPosition(pottedUp);

            TextView textView = (TextView) pottedPlantTextRenderable.getView();
            textView.setText("Please water this plant!");
        }

        return anchorNode;
    }

    private AnchorNode renderBed(HitResult hitResult) {
        AnchorNode anchorNode = null;

        if (bedRenderable != null) {
            Anchor anchor = hitResult.createAnchor();
            anchorNode = new AnchorNode(anchor);

            bedNode.setParent(anchorNode);
            Node bed = new Node();
            bed.setParent(bedNode);
            bed.setRenderable(bedRenderable);
            bed.setLocalScale(new Vector3(0.25f, 0.25f, 0.25f));
        }

        return anchorNode;
    }

    private AnchorNode renderCouch(HitResult hitResult) {
        AnchorNode anchorNode = null;

        if (couchRenderable != null) {
            Anchor anchor = hitResult.createAnchor();
            anchorNode = new AnchorNode(anchor);

            couchNode.setParent(anchorNode);
            Node couch = new Node();
            couch.setParent(couchNode);
            couch.setRenderable(couchRenderable);
            couch.setLocalScale(new Vector3(0.25f, 0.25f, 0.25f));
        }

        return anchorNode;
    }

    private AnchorNode renderDesk(HitResult hitResult) {
        AnchorNode anchorNode = null;

        if (deskRenderable != null) {
            Anchor anchor = hitResult.createAnchor();
            anchorNode = new AnchorNode(anchor);

            deskNode.setParent(anchorNode);
            Node desk = new Node();
            desk.setParent(deskNode);
            desk.setRenderable(deskRenderable);
            desk.setLocalScale(new Vector3(0.25f, 0.25f, 0.25f));
        }

        return anchorNode;
    }

    private AnchorNode renderOfficeChair(HitResult hitResult) {
        AnchorNode anchorNode = null;

        if (officeChairRenderable != null) {
            Anchor anchor = hitResult.createAnchor();
            anchorNode = new AnchorNode(anchor);

            officeChairNode.setParent(anchorNode);
            Node officeChair = new Node();
            officeChair.setParent(officeChairNode);
            officeChair.setRenderable(officeChairRenderable);
            officeChair.setLocalScale(new Vector3(0.25f, 0.25f, 0.25f));
        }

        return anchorNode;
    }
}

