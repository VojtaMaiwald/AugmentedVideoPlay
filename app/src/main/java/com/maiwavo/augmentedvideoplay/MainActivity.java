package com.maiwavo.augmentedvideoplay;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.Collection;

public class MainActivity extends AppCompatActivity {

    private ExternalTexture texture;
    private MediaPlayer mediaPlayer;
    private MyARFragment arFragment;
    private Scene scene;
    private ModelRenderable renderable;
    private boolean isDetected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.texture = new ExternalTexture();
        this.mediaPlayer = MediaPlayer.create(this, R.raw.matrix);
        this.mediaPlayer.setSurface(texture.getSurface());
        this.mediaPlayer.setLooping(true);

        ModelRenderable.builder()
                .setSource(this, Uri.parse("video_screen.sfb"))
                .build()
                .thenAccept(modelRenderable -> {
                    modelRenderable.getMaterial().setExternalTexture("videoTexture", texture);
                    modelRenderable.getMaterial().setFloat4("keyColor", new Color(0.01843f, 1f, 0.098f));

                    renderable = modelRenderable;
                });

        this.arFragment = (MyARFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        this.scene = this.arFragment.getArSceneView().getScene();

        this.scene.addOnUpdateListener(this::OnUpdate);
    }

    private void OnUpdate(FrameTime frameTime) {
        if (isDetected) {
            return;
        }

        Frame frame = arFragment.getArSceneView().getArFrame();

        Collection<AugmentedImage> images = frame.getUpdatedTrackables(AugmentedImage.class);

        for (AugmentedImage image : images) {
            if (image.getTrackingState() == TrackingState.TRACKING) {
                if (image.getName().equals("matrix")) {
                    isDetected = true;

                    playVideo(image.createAnchor(image.getCenterPose()), image.getExtentX(), image.getExtentZ());
                }
            }
        }
    }

    private void playVideo(Anchor anchor, float extentX, float extentZ) {
        Log.println(Log.ASSERT, "lol", mediaPlayer.isPlaying() + "");
        mediaPlayer.start();
        Log.println(Log.ASSERT, "lol", mediaPlayer.isPlaying() + "");
        AnchorNode node = new AnchorNode(anchor);
        texture.getSurfaceTexture().setOnFrameAvailableListener(surfaceTexture -> {
            node.setRenderable(renderable);
            texture.getSurfaceTexture().setOnFrameAvailableListener(null);
        });
        node.setWorldScale(new Vector3(extentX, 1f, extentZ));
        scene.addChild(node);
    }
}