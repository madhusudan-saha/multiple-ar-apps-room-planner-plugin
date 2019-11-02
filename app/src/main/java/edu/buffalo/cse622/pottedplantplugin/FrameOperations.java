package edu.buffalo.cse622.pottedplantplugin;

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

    public ViewRenderable textRenderable;
    public ModelRenderable pottedPlantRenderable;
    public SkeletonNode pottedPlant;
    public Node node;

    public FrameOperations(Resources dynamicResources, Context context) {
        int layoutId = dynamicResources.getIdentifier("text_view", "layout", "edu.buffalo.cse622.pottedplantplugin");
        XmlResourceParser textViewXml = dynamicResources.getLayout(layoutId);
        View view = LayoutInflater.from(context).inflate(textViewXml, null);

        ViewRenderable.builder()
                .setView(context, view)
                .build()
                .thenAccept(
                        (renderable) -> {
                            textRenderable = renderable;
                        });

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
        /*
        CompletableFuture<ModelRenderable> pottedPlantStage =
                ModelRenderable.builder().setSource(context, Uri.parse("potted_plant.sfb")).build();
        */
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

    public AnchorNode processFrame(Frame frame) {
        //Node node = null;
        AnchorNode anchorNode = null;
        for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
            if (textRenderable != null) {
                Log.d("Check Model: ", pottedPlantRenderable.getMaterial().toString());
                Anchor anchor = plane.createAnchor(plane.getCenterPose());
                anchorNode = new AnchorNode(anchor);

            /*
            node = new Node();
            node.setParent(anchorNode);
            node.setRenderable(textRenderable);
            TextView textView = (TextView) textRenderable.getView();
            textView.setText("Works!");
            */
                // Create potted plant relative to anchor node
                pottedPlant.setParent(anchorNode);
                pottedPlant.setRenderable(pottedPlantRenderable);
                pottedPlant.setLocalScale(new Vector3(0.25f, 0.25f, 0.25f));

                // Attach bone node
                Node boneNode = new Node();
                boneNode.setParent(pottedPlant);
                pottedPlant.setBoneAttachment("Potted Plant", boneNode);

                /*
                // Make potted plant face camera
                Vector3 cameraPosition = anchorNode.getScene().getCamera().getWorldPosition();
                Vector3 cardPosition = pottedPlant.getWorldPosition();
                Vector3 direction = Vector3.subtract(cameraPosition, cardPosition);
                Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
                // Z rotation set to zero so potted plant doesn't look upwards
                lookRotation.z = 0f;
                pottedPlant.setWorldRotation(lookRotation);
                */

                // Use bone node to display ViewRenderable
                node.setRenderable(textRenderable);
                node.setParent(boneNode);

                // Adjustments to the text position relative to the potted plant
                Vector3 pottedUp = pottedPlant.getUp();
                pottedUp.z += .4f;
                pottedUp.y += 2f;
                node.setLocalPosition(pottedUp);

                TextView textView = (TextView) textRenderable.getView();
                textView.setText("Plane detected: " + plane.getType().toString());

                //transformableNode.setLocalRotation(Quaternion.axisAngle(new Vector3(1f, 0, 0), 270f));
            }

            break;
        }

        return anchorNode;
    }
}

