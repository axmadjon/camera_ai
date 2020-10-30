package uz.datalab.camera_ai

import android.app.Activity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener
import io.flutter.view.TextureRegistry
import uz.datalab.vision.Vision
import uz.datalab.vision.VisionUtil

/**
 * Platform implementation of the camera_plugin.
 *
 * <p>Instantiate this in an add to app scenario to gracefully handle activity and context changes.
 * See {@code io.flutter.plugins.camera.MainActivity} for an example.
 *
 * <p>Call {@link #registerWith(Registrar)} to register an implementation of this that uses the
 * stable {@code io.flutter.plugin.common} package.
 */
class CameraAiPlugin : FlutterPlugin, ActivityAware {

    companion object {
        /**
         * Registers a plugin implementation that uses the stable `io.flutter.plugin.common`
         * package.
         *
         *
         * Calling this automatically initializes the plugin. However plugins initialized this way
         * won't react to changes in activity or context, unlike [CameraAiPlugin].
         */

        open fun registerWith(registrar: Registrar) {
            val plugin = CameraAiPlugin()
            plugin.maybeStartListening(
                    registrar.activity(),
                    registrar.messenger(),
                    CameraPermissions.PermissionsRegistry { listener: RequestPermissionsResultListener ->
                        registrar.addRequestPermissionsResultListener(listener)
                    },
                    registrar.view())
            plugin.initializeVision(registrar.activity())
        }

    }

    private var flutterPluginBinding: FlutterPluginBinding? = null
    private var methodCallHandler: MethodCallHandlerImpl? = null

    override fun onAttachedToEngine(binding: FlutterPluginBinding) {
        flutterPluginBinding = binding
    }

    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
        flutterPluginBinding = null
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        maybeStartListening(
                binding.activity,
                flutterPluginBinding!!.binaryMessenger,
                CameraPermissions.PermissionsRegistry { listener: RequestPermissionsResultListener -> binding.addRequestPermissionsResultListener(listener) },
                flutterPluginBinding!!.textureRegistry)
        initializeVision(binding.activity)
    }

    override fun onDetachedFromActivity() {
        methodCallHandler?.stopListening()
        methodCallHandler = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    private fun maybeStartListening(
            activity: Activity,
            messenger: BinaryMessenger,
            permissionsRegistry: CameraPermissions.PermissionsRegistry,
            textureRegistry: TextureRegistry) {
        val cameraPermissions = CameraPermissions()
        methodCallHandler = MethodCallHandlerImpl(
                activity, messenger, cameraPermissions, permissionsRegistry, textureRegistry)
    }

    private fun initializeVision(activity: Activity) {
        Vision.init(activity) {
            try {
                VisionUtil.copyBigDataToSD(activity, "det1.bin")
                VisionUtil.copyBigDataToSD(activity, "det2.bin")
                VisionUtil.copyBigDataToSD(activity, "det3.bin")
                VisionUtil.copyBigDataToSD(activity, "det1.param")
                VisionUtil.copyBigDataToSD(activity, "det2.param")
                VisionUtil.copyBigDataToSD(activity, "det3.param")
                VisionUtil.copyBigDataToSD(activity, "mobilefacenet.param")
                VisionUtil.copyBigDataToSD(activity, "mobilefacenet.bin")
                return@init true
            } catch (e: Exception) {
                e.printStackTrace()
                return@init false
            }
        }
    }
}
