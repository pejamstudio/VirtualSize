package com.example.virtualsize

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import java.util.concurrent.CompletableFuture

class MyARNode(context: Context, modeId: Int) : AnchorNode() {

    var image: AugmentedImage? = null
        @RequiresApi(api = Build.VERSION_CODES.N)
        set(image) {
            field = image
            if (modelRenderableCompletableFuture!!.isDone) {
                CompletableFuture.allOf(modelRenderableCompletableFuture)
                    .thenAccept { aVoid: Void -> this@MyARNode.image = image }.exceptionally { throwable -> null }
            }

            anchor = image!!.createAnchor(image.getCenterPose())

            val node = Node()
            val pose = Pose.makeTranslation(0.0f, 0.0f, 0.25f)

            node.setParent(this)
            node.localPosition = Vector3(pose.tx(), pose.ty(), pose.tz())
            node.localRotation = Quaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                node.renderable = modelRenderableCompletableFuture!!.getNow(null)
            }
        }

    init {
        if (modelRenderableCompletableFuture == null) {
            modelRenderableCompletableFuture = ModelRenderable.builder()
                .setRegistryId("My Model")
                .setSource(context, modeId)
                .build()
        }
    }

    companion object {
        private var modelRenderableCompletableFuture: CompletableFuture<ModelRenderable>? = null
    }
}