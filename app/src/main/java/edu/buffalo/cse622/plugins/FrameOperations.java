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
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FrameOperations {

    private static final String TAG = "RoomPlannerPlugin:" + FrameOperations.class.getSimpleName();

    private Resources dynamicResources;
    private ArFragment arFragment;
    private Context context;
    private HashSet<AnchorNode> pluginObjects;

    private AnchorNode pottedPlantAnchorNode;
    private ViewRenderable pottedPlantTextRenderable;
    private ModelRenderable pottedPlantRenderable;
    private TransformableNode pottedPlantNode;

    private AnchorNode bedAnchorNode;
    private ModelRenderable bedRenderable;
    private TransformableNode bedNode;

    private AnchorNode couchAnchorNode;
    private ModelRenderable couchRenderable;
    private TransformableNode couchNode;

    private AnchorNode deskAnchorNode;
    private ModelRenderable deskRenderable;
    private TransformableNode deskNode;

    private AnchorNode officeChairAnchorNode;
    private ModelRenderable officeChairRenderable;
    private TransformableNode officeChairNode;

    /**
     * Constructor does all the resources loading that the plugin requires.
     *
     * @param dynamicResources The Resources object is already initialized and passed by MetaApp which helps the plugin to be "aware" of its own resources.
     * @param arFragment       ArFragment object passed by MetaApp.
     */
    public FrameOperations(Resources dynamicResources, ArFragment arFragment, HashSet<AnchorNode> pluginObjects) {
        this.dynamicResources = dynamicResources;
        this.arFragment = arFragment;
        this.context = arFragment.getContext();
        this.pluginObjects = pluginObjects;

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
                ModelRenderable.builder().setSource(context, () -> {
                    InputStream inputStream = null;
                    try {
                        AssetManager assetManager = dynamicResources.getAssets();
                        inputStream = assetManager.open("potted_plant.sfb");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return inputStream;
                }).build();
        CompletableFuture<ModelRenderable> bedStage =
                ModelRenderable.builder().setSource(context, () -> {
                    InputStream inputStream = null;
                    try {
                        AssetManager assetManager = dynamicResources.getAssets();
                        inputStream = assetManager.open("bed.sfb");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return inputStream;
                }).build();
        CompletableFuture<ModelRenderable> couchStage =
                ModelRenderable.builder().setSource(context, () -> {
                    InputStream inputStream = null;
                    try {
                        AssetManager assetManager = dynamicResources.getAssets();
                        inputStream = assetManager.open("couch.sfb");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return inputStream;
                }).build();
        CompletableFuture<ModelRenderable> deskStage =
                ModelRenderable.builder().setSource(context, () -> {
                    InputStream inputStream = null;
                    try {
                        AssetManager assetManager = dynamicResources.getAssets();
                        inputStream = assetManager.open("desk.sfb");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return inputStream;
                }).build();
        CompletableFuture<ModelRenderable> officeChairStage =
                ModelRenderable.builder().setSource(context, () -> {
                    InputStream inputStream = null;
                    try {
                        AssetManager assetManager = dynamicResources.getAssets();
                        inputStream = assetManager.open("office_chair.sfb");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return inputStream;
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
     * This is where we do most of our operations on the frame.
     *
     * @param frame
     * @return
     */
    private void processFrame(Frame frame) {
    }

    /**
     * This is the method that is invoked when user input for this plugin is activated in MetaApp and user taps on a plane.
     *
     * @param hitResult
     */
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
                        renderObject(renderPottedPlant(hitResult));

                        break;

                    case "Bed":
                        renderObject(renderBed(hitResult));

                        break;

                    case "Couch":
                        renderObject(renderCouch(hitResult));

                        break;

                    case "Desk":
                        renderObject(renderDesk(hitResult));

                        break;

                    case "Office Chair":
                        renderObject(renderOfficeChair(hitResult));

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

    /**
     * This is invoked when the MetaApp clears or disables this plugin.
     *
     */
    private void onDestroy() {
    }

    /*
     * Separate methods for object rendering in case we want to do any object specific configuration later.
     */

    private AnchorNode renderPottedPlant(HitResult hitResult) {
        if (pottedPlantRenderable != null && pottedPlantTextRenderable != null) {
            Anchor anchor = hitResult.createAnchor();
            if (pottedPlantAnchorNode == null) {
                pottedPlantAnchorNode = new AnchorNode(anchor);
            }
            else {
                pottedPlantAnchorNode.setAnchor(anchor);
            }

            // Create potted plant relative to anchor node
            pottedPlantNode.setParent(pottedPlantAnchorNode);
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

        return pottedPlantAnchorNode;
    }

    private AnchorNode renderBed(HitResult hitResult) {
        if (bedRenderable != null) {
            Anchor anchor = hitResult.createAnchor();
            if (bedAnchorNode == null) {
                bedAnchorNode = new AnchorNode(anchor);
            }
            else {
                bedAnchorNode.setAnchor(anchor);
            }

            bedNode.setParent(bedAnchorNode);
            Node bed = new Node();
            bed.setParent(bedNode);
            bed.setRenderable(bedRenderable);
            bed.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));
        }

        return bedAnchorNode;
    }

    private AnchorNode renderCouch(HitResult hitResult) {
        if (couchRenderable != null) {
            Anchor anchor = hitResult.createAnchor();
            if (couchAnchorNode == null) {
                couchAnchorNode = new AnchorNode(anchor);
            }
            else {
                couchAnchorNode.setAnchor(anchor);
            }

            couchNode.setParent(couchAnchorNode);
            Node couch = new Node();
            couch.setParent(couchNode);
            couch.setRenderable(couchRenderable);
            couch.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));
        }

        return couchAnchorNode;
    }

    private AnchorNode renderDesk(HitResult hitResult) {
        if (deskRenderable != null) {
            Anchor anchor = hitResult.createAnchor();
            if (deskAnchorNode == null) {
                deskAnchorNode = new AnchorNode(anchor);
            }
            else {
                deskAnchorNode.setAnchor(anchor);
            }

            deskNode.setParent(deskAnchorNode);
            Node desk = new Node();
            desk.setParent(deskNode);
            desk.setRenderable(deskRenderable);
            desk.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));
        }

        return deskAnchorNode;
    }

    private AnchorNode renderOfficeChair(HitResult hitResult) {
        if (officeChairRenderable != null) {
            Anchor anchor = hitResult.createAnchor();
            if (officeChairAnchorNode == null) {
                officeChairAnchorNode = new AnchorNode(anchor);
            }
            else {
                officeChairAnchorNode.setAnchor(anchor);
            }

            officeChairNode.setParent(officeChairAnchorNode);
            Node officeChair = new Node();
            officeChair.setParent(officeChairNode);
            officeChair.setRenderable(officeChairRenderable);
            officeChair.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));
        }

        return officeChairAnchorNode;
    }

    private void renderObject(AnchorNode anchorNode) {
        if (anchorNode != null) {
            anchorNode.setParent(arFragment.getArSceneView().getScene());
            pluginObjects.add(anchorNode);
        }
    }
}

