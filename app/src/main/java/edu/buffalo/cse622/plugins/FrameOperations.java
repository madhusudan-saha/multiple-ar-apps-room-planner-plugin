package edu.buffalo.cse622.plugins;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.SkeletonNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FrameOperations {

    private ViewRenderable textRenderable;
    private ModelRenderable pottedPlantRenderable;
    private SkeletonNode pottedPlant;
    private Node node;
    private boolean userTap;

    /**
     * Constructor does all the resources loading that the plugin requires.
     *
     * @param dynamicResources The Resources object is already initialized and passed by MetaApp which helps the plugin to be "aware" of its own resources.
     * @param context          This is the Context object passed by the MetaApp.
     */
    public FrameOperations(Context context, Resources dynamicResources) {
        // This is how we load a layout resource.
        int layoutId = dynamicResources.getIdentifier("text_view", "layout", "edu.buffalo.cse622.plugins");
        XmlResourceParser textViewXml = dynamicResources.getLayout(layoutId);
        View view = LayoutInflater.from(context).inflate(textViewXml, null);

        ViewRenderable.builder()
                .setView(context, view)
                .build()
                .thenAccept(
                        (renderable) -> {
                            textRenderable = renderable;
                        });

        // This is how we load a model/asset.
        CompletableFuture<ModelRenderable> pottedPlantStage =
                ModelRenderable.builder().setSource(context, new Callable() {
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

        CompletableFuture.allOf(
                pottedPlantStage)
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
                            } catch (InterruptedException | ExecutionException ex) {
                                Log.d("handle", "Unable to load renderable", ex);
                            }

                            return null;
                        });

        pottedPlant = new SkeletonNode();
        node = new Node();
    }

    /**
     * This is where we do most of our operations on the frame and return the AnchorNode object back to MetaApp.
     *
     * @param frame
     * @return
     */
    private AnchorNode processFrame(Frame frame) {

        if (userTap) {
            return null;
        }

        AnchorNode anchorNode = null;
        for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
            if (pottedPlantRenderable != null && textRenderable != null) {
                Anchor anchor = plane.createAnchor(plane.getCenterPose());
                anchorNode = new AnchorNode(anchor);

                // Create potted plant relative to anchor node
                pottedPlant.setParent(anchorNode);
                pottedPlant.setRenderable(pottedPlantRenderable);
                pottedPlant.setLocalScale(new Vector3(0.25f, 0.25f, 0.25f));

                // Attach bone node
                Node boneNode = new Node();
                boneNode.setParent(pottedPlant);
                pottedPlant.setBoneAttachment("Potted Plant", boneNode);

                // Use bone node to display ViewRenderable
                node.setRenderable(textRenderable);
                node.setParent(boneNode);

                // Adjustments to the text position relative to the potted plant
                Vector3 pottedUp = pottedPlant.getUp();
                pottedUp.z += .4f;
                pottedUp.y += 2f;
                node.setLocalPosition(pottedUp);

                TextView textView = (TextView) textRenderable.getView();
                textView.setText("Please water this plant!");
            }

            break;
        }

        return anchorNode;
    }

    private AnchorNode planeTap(HitResult hitResult) {
        AnchorNode anchorNode = null;

        if (pottedPlantRenderable != null && textRenderable != null) {
            Anchor anchor = hitResult.createAnchor();
            anchorNode = new AnchorNode(anchor);

            // Create potted plant relative to anchor node
            pottedPlant.setParent(anchorNode);
            pottedPlant.setRenderable(pottedPlantRenderable);
            pottedPlant.setLocalScale(new Vector3(0.25f, 0.25f, 0.25f));

            // Attach bone node
            Node boneNode = new Node();
            boneNode.setParent(pottedPlant);
            pottedPlant.setBoneAttachment("Potted Plant", boneNode);

            // Use bone node to display ViewRenderable
            node.setRenderable(textRenderable);
            node.setParent(boneNode);

            // Adjustments to the text position relative to the potted plant
            Vector3 pottedUp = pottedPlant.getUp();
            pottedUp.z += .4f;
            pottedUp.y += 2f;
            node.setLocalPosition(pottedUp);

            TextView textView = (TextView) textRenderable.getView();
            textView.setText("Please water this plant!");

            userTap = true;
        }

        return anchorNode;
    }
}

